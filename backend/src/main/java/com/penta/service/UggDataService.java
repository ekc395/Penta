package com.penta.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UggDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(UggDataService.class);
    
    // HTTP Headers Constants
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                             "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String ACCEPT_HEADER = "text/html,application/xhtml+xml,application/xml";
    private static final String ACCEPT_LANGUAGE = "en-US,en;q=0.9";
    
    @Value("${ugg.base-url}")
    private String baseUrl;
    
    @Value("${ugg.timeout}")
    private int timeout;
    
    private final Map<String, Map<String, Double>> synergyCache = new ConcurrentHashMap<>();
    private final Map<String, List<CounterData>> goodMatchupsCache = new ConcurrentHashMap<>();
    
    // Simple rate limiting - tracks last request time per domain
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 1000; // 1 second between requests
    
    /**
     * Get good matchups for a champion (champions that struggle against it)
     * Scrapes data from the "Worst Picks" column on U.GG
     */
    public Optional<List<CounterData>> getGoodMatchups(String championName) {
        if (goodMatchupsCache.containsKey(championName)) {
            return Optional.of(goodMatchupsCache.get(championName));
        }
        
        try {
            List<CounterData> goodMatchups = scrapeWorstPicks(championName);
            goodMatchupsCache.put(championName, goodMatchups);
            return Optional.of(goodMatchups);
        } catch (IOException e) {
            logger.error("Failed to scrape good matchups for {}: {}", championName, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.error("Rate limiting interrupted for {}", championName);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
    
    /**
     * Get synergy data between champions
     */
    public Optional<Map<String, Double>> getChampionSynergy(String championName) {
        if (synergyCache.containsKey(championName)) {
            return Optional.of(synergyCache.get(championName));
        }
        
        try {
            Map<String, Double> synergyData = scrapeSynergyData(championName);
            synergyCache.put(championName, synergyData);
            return Optional.of(synergyData);
        } catch (IOException e) {
            logger.error("Failed to scrape synergy data for {}: {}", championName, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.error("Rate limiting interrupted for {}", championName);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
    
    /**
     * Get champion tier list data
     */
    public Optional<Map<String, Integer>> getChampionTierList(String role) {
        try {
            Map<String, Integer> tierList = scrapeTierListData(role);
            return Optional.of(tierList);
        } catch (IOException e) {
            logger.error("Failed to scrape tier list for role {}: {}", role, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.error("Rate limiting interrupted for tier list: {}", role);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
    
    /**
     * Get champion win rates and pick rates (DONT THINK WE NEED PICK RATES HERE)
     */
    public Optional<Map<String, ChampionStats>> getChampionStats(String role) {
        try {
            Map<String, ChampionStats> stats = scrapeChampionStats(role);
            return Optional.of(stats);
        } catch (IOException e) {
            logger.error("Failed to scrape champion stats for role {}: {}", role, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.error("Rate limiting interrupted for stats: {}", role);
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
    
    /**
     * Clear all caches
     */
    public void clearCache() {
        synergyCache.clear();
        goodMatchupsCache.clear();
    }
    
    /**
     * Clear cache for specific champion
     */
    public void clearCacheForChampion(String championName) {
        synergyCache.remove(championName);
        goodMatchupsCache.remove(championName);
    }
    
    /**
     * Simple rate limiting - ensures minimum time between requests
     */
    private synchronized void rateLimit() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
            Thread.sleep(sleepTime);
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
    
    /**
     * Creates a Jsoup connection with standard headers and rate limiting
     */
    private Document fetchDocument(String url) throws IOException, InterruptedException {
        rateLimit();
        
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", ACCEPT_HEADER)
                .header("Accept-Language", ACCEPT_LANGUAGE)
                .timeout(timeout)
                .get();
    }
    
    /**
     * Scrapes worst picks data from U.GG counter page using CSS selectors
     */
    private List<CounterData> scrapeWorstPicks(String championName) throws IOException, InterruptedException {
        List<CounterData> worstPicks = new ArrayList<>();
        
        String url = baseUrl + "/lol/champions/" + championName.toLowerCase() + "/counter";
        
        Document doc = fetchDocument(url);
        
        // Try multiple potential CSS selectors for worst picks section
        Elements worstPickRows = doc.select(".worst-picks .champion-row, .worst-matchups .row, [data-worst-picks] .champion");
        
        if (worstPickRows.isEmpty()) {
            logger.warn("No worst picks elements found using CSS selectors for {}, falling back to text parsing", championName);
            return parseWorstPicksFromText(doc);
        }
        
        // Parse using CSS selectors
        for (Element row : worstPickRows) {
            try {
                String champName = row.select(".champion-name, .name").text();
                String winRateText = row.select(".win-rate, .wr, [data-win-rate]").text().replace("%", "").trim();
                String gamesText = row.select(".games, .matches, [data-games]").text().replace(",", "").trim();
                
                if (!champName.isEmpty() && !winRateText.isEmpty()) {
                    double winRate = Double.parseDouble(winRateText);
                    int games = gamesText.isEmpty() ? 0 : Integer.parseInt(gamesText);
                    
                    worstPicks.add(new CounterData(champName, winRate, games));
                }
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse row data: {}", e.getMessage());
            }
        }
        
        return worstPicks;
    }
    
    /**
     * Fallback text parsing method if CSS selectors fail (NEED TO EITHER FIND A WAY FOR CSS SELECTORS NOT TO FAIL OR MAKE THIS MORE EFFICIENT)
     */
    private List<CounterData> parseWorstPicksFromText(Document doc) {
        List<CounterData> worstPicks = new ArrayList<>();
        String text = doc.body().text();
        
        int worstPicksIndex = text.indexOf("Worst Picks vs");
        int bestLaneIndex = text.indexOf("Best Lane Counters", worstPicksIndex);
        
        if (worstPicksIndex == -1 || bestLaneIndex == -1) {
            logger.warn("Could not find 'Worst Picks' section in page text");
            return worstPicks;
        }
        
        String worstPicksSection = text.substring(worstPicksIndex, bestLaneIndex);
        String[] lines = worstPicksSection.split("(?=\\d+\\.\\d+% WR)");
        
        for (String line : lines) {
            if (line.contains("% WR") && line.contains("games")) {
                String[] parts = line.trim().split("\\s+");
                
                if (parts.length >= 4) {
                    int wrIndex = -1;
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].endsWith("%")) {
                            wrIndex = i;
                            break;
                        }
                    }
                    
                    if (wrIndex > 0 && wrIndex + 2 < parts.length) {
                        StringBuilder champName = new StringBuilder();
                        for (int i = 0; i < wrIndex; i++) {
                            if (champName.length() > 0) champName.append(" ");
                            champName.append(parts[i]);
                        }
                        
                        String wr = parts[wrIndex].replace("%", "");
                        String games = parts[wrIndex + 2];
                        
                        if (!champName.toString().isEmpty()) {
                            try {
                                double winRate = Double.parseDouble(wr);
                                int gamesPlayed = Integer.parseInt(games);
                                
                                worstPicks.add(new CounterData(
                                    champName.toString().trim(),
                                    winRate,
                                    gamesPlayed
                                ));
                            } catch (NumberFormatException e) {
                                logger.warn("Failed to parse numeric data: {}", e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        
        return worstPicks;
    }
    
    private Map<String, Double> scrapeSynergyData(String championName) throws IOException, InterruptedException {
        Map<String, Double> synergyData = new HashMap<>();
        
        String url = baseUrl + "/lol/champions/" + championName.toLowerCase() + "/synergy";
        
        Document doc = fetchDocument(url);
        
        // Use CSS selectors for synergy data
        Elements synergyElements = doc.select(".synergy-item, .duo-row, [data-synergy] .champion");
        
        for (Element element : synergyElements) {
            try {
                String champion = element.select(".champion-name, .name").text();
                String winRateText = element.select(".win-rate, .wr").text().replace("%", "").trim();
                
                if (!champion.isEmpty() && !winRateText.isEmpty()) {
                    double winRate = Double.parseDouble(winRateText);
                    synergyData.put(champion, winRate);
                }
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse synergy data: {}", e.getMessage());
            }
        }
        
        if (synergyData.isEmpty()) {
            logger.warn("No synergy data found for {}", championName);
        }
        
        return synergyData;
    }
    
    private Map<String, Integer> scrapeTierListData(String role) throws IOException, InterruptedException {
        Map<String, Integer> tierList = new HashMap<>();
        
        String url = baseUrl + "/lol/tier-list?role=" + role.toLowerCase();
        
        Document doc = fetchDocument(url);
        
        // Use CSS selectors for tier list
        Elements tierElements = doc.select(".tier-list-row, [data-tier] .champion");
        
        for (Element element : tierElements) {
            try {
                String champion = element.select(".champion-name, .name").text();
                String tierText = element.select(".tier, [data-tier]").text().trim();
                
                if (!champion.isEmpty() && !tierText.isEmpty()) {
                    int tier = Integer.parseInt(tierText);
                    tierList.put(champion, tier);
                }
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse tier data: {}", e.getMessage());
            }
        }
        
        return tierList;
    }
    
    private Map<String, ChampionStats> scrapeChampionStats(String role) throws IOException, InterruptedException {
        Map<String, ChampionStats> stats = new HashMap<>();
        
        String url = baseUrl + "/lol/tier-list?role=" + role.toLowerCase();
        
        Document doc = fetchDocument(url);
        
        // Use CSS selectors for champion stats
        Elements statElements = doc.select(".champion-row, [data-champion-stats]");
        
        for (Element element : statElements) {
            try {
                String champion = element.select(".champion-name, .name").text();
                String wrText = element.select(".win-rate, .wr").text().replace("%", "").trim();
                String prText = element.select(".pick-rate, .pr").text().replace("%", "").trim();
                String brText = element.select(".ban-rate, .br").text().replace("%", "").trim();
                String tierText = element.select(".tier").text().trim();
                
                if (!champion.isEmpty() && !wrText.isEmpty()) {
                    double winRate = Double.parseDouble(wrText);
                    double pickRate = prText.isEmpty() ? 0.0 : Double.parseDouble(prText);
                    double banRate = brText.isEmpty() ? 0.0 : Double.parseDouble(brText);
                    int tier = tierText.isEmpty() ? 0 : Integer.parseInt(tierText);
                    
                    stats.put(champion, new ChampionStats(winRate, pickRate, banRate, tier));
                }
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse champion stats: {}", e.getMessage());
            }
        }
        
        return stats;
    }
    
    /**
     * Data class for counter/worst pick information
     */
    public static class CounterData {
        private String championName;
        private double winRate;
        private int gamesPlayed;
        
        public CounterData(String championName, double winRate, int gamesPlayed) {
            this.championName = championName;
            this.winRate = winRate;
            this.gamesPlayed = gamesPlayed;
        }
        
        public String getChampionName() {
            return championName;
        }
        
        public void setChampionName(String championName) {
            this.championName = championName;
        }
        
        public double getWinRate() {
            return winRate;
        }
        
        public void setWinRate(double winRate) {
            this.winRate = winRate;
        }
        
        public int getGamesPlayed() {
            return gamesPlayed;
        }
        
        public void setGamesPlayed(int gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }
        
        @Override
        public String toString() {
            return String.format("%s - WR: %.2f%% (%d games)", 
                championName, winRate, gamesPlayed);
        }
    }
    
    public static class ChampionStats {
        public double winRate;
        public double pickRate;
        public double banRate;
        public int tier;
        
        public ChampionStats(double winRate, double pickRate, double banRate, int tier) {
            this.winRate = winRate;
            this.pickRate = pickRate;
            this.banRate = banRate;
            this.tier = tier;
        }
    }
}