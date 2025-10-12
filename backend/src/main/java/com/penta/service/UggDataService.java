package com.penta.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import com.penta.dto.CounterData;
import com.penta.dto.ChampionStats;

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
    
    @Value("${ugg.timeout:5000}")
    private int timeout;
    
    @Value("${ugg.max-retries:3}")
    private int maxRetries;
    
    // Rate limiter using semaphore
    private final Semaphore rateLimiter = new Semaphore(1);
    private volatile Instant lastRequestTime = Instant.now();
    private static final Duration MIN_REQUEST_INTERVAL = Duration.ofMillis(1000);

    // Exponential backoff
    private static long BASE_DELAY_MS = 1000;
    
    /**
     * Get good matchups for a champion with caching
     */
    @Cacheable(value = "goodMatchups", key = "#championName")
    public Optional<List<CounterData>> getGoodMatchups(String championName) {
        try {
            List<CounterData> goodMatchups = scrapeWorstPicksWithRetry(championName);
            return Optional.of(goodMatchups);
        } catch (Exception e) {
            logger.error("Failed to fetch good matchups for {}: {}", championName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get synergy data with caching
     */
    @Cacheable(value = "synergy", key = "#championName")
    public Optional<Map<String, Double>> getChampionSynergy(String championName) {
        try {
            Map<String, Double> synergyData = scrapeSynergyDataWithRetry(championName);
            return Optional.of(synergyData);
        } catch (Exception e) {
            logger.error("Failed to fetch synergy for {}: {}", championName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get champion tier list data
     */
    @Cacheable(value = "tierList", key = "#role")
    public Optional<Map<String, Integer>> getChampionTierList(String role) {
        try {
            Map<String, Integer> tierList = scrapeTierListDataWithRetry(role);
            return Optional.of(tierList);
        } catch (Exception e) {
            logger.error("Failed to fetch tier list for role {}: {}", role, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Get champion win rates
     */
    @Cacheable(value = "championStats", key = "#role")
    public Optional<Map<String, ChampionStats>> getChampionStats(String role) {
        try {
            Map<String, ChampionStats> stats = scrapeChampionStatsWithRetry(role);
            return Optional.of(stats);
        } catch (Exception e) {
            logger.error("Failed to fetch champion stats for role {}: {}", role, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Scrape with retry logic
     */
    private List<CounterData> scrapeWorstPicksWithRetry(String championName) throws IOException, InterruptedException {
        return retryOperation(() -> scrapeWorstPicks(championName), "worst picks for " + championName);
    }
    
    private Map<String, Double> scrapeSynergyDataWithRetry(String championName) throws IOException, InterruptedException {
        return retryOperation(() -> scrapeSynergyData(championName), "synergy for " + championName);
    }
    
    private Map<String, Integer> scrapeTierListDataWithRetry(String role) throws IOException, InterruptedException {
        return retryOperation(() -> scrapeTierListData(role), "tier list for " + role);
    }
    
    private Map<String, ChampionStats> scrapeChampionStatsWithRetry(String role) throws IOException, InterruptedException {
        return retryOperation(() -> scrapeChampionStats(role), "stats for " + role);
    }
    
    /**
     * Generic retry operation with exponential backoff
     */
    private <T> T retryOperation(SupplierWithException<T> operation, String operationName) 
            throws IOException, InterruptedException {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            try {
                return operation.get();
            } catch (IOException e) {
                lastException = e;
                attempt++;
                if (attempt < maxRetries) {
                    long backoffMs = (long) Math.pow(2, attempt) * BASE_DELAY_MS;
                    Thread.sleep(backoffMs);
                }
            }
        }
        
        throw new IOException("Failed after " + maxRetries + " retries for " + operationName, lastException);
    }
    
    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws IOException, InterruptedException;
    }
    
    /**
     * Rate limiting with token bucket
     */
    private void rateLimit() throws InterruptedException {
        rateLimiter.acquire();
        try {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(lastRequestTime, now);
            
            if (elapsed.compareTo(MIN_REQUEST_INTERVAL) < 0) {
                Duration sleepDuration = MIN_REQUEST_INTERVAL.minus(elapsed);
                Thread.sleep(sleepDuration.toMillis());
            }
            
            lastRequestTime = Instant.now();
        } finally {
            rateLimiter.release();
        }
    }
    
    /**
     * Fetch document with proper error handling
     */
    private Document fetchDocument(String url) throws IOException, InterruptedException {
        rateLimit();
        
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept", ACCEPT_HEADER)
                    .header("Accept-Language", ACCEPT_LANGUAGE)
                    .header("Cache-Control", "no-cache")
                    .timeout(timeout)
                    .followRedirects(true)
                    .maxBodySize(0)
                    .get();
        } catch (IOException e) {
            logger.error("Failed to fetch {}: {}", url, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Scrape worst picks using precise CSS selectors based on U.GG's actual HTML structure
     * Structure: a.flex.items-center.p-[12px] contains the champion row data
     */
    private List<CounterData> scrapeWorstPicks(String championName) throws IOException, InterruptedException {
        String url = baseUrl + "/lol/champions/" + championName.toLowerCase() + "/counter";
        Document doc = fetchDocument(url);

        Elements championRows = doc.select("a.flex.items-center[class*=p-\\[12px\\]]");
        
        if (championRows.isEmpty()) {
            championRows = doc.select("a:has(div.text-white.font-bold.truncate)");
        }
        
        if (championRows.isEmpty()) {
            logger.error("No worst picks data found for {}", championName);
            return Collections.emptyList();
        }
        
        List<CounterData> worstPicks = new ArrayList<>();
        
        for (Element row : championRows) {
            try {
                Element nameElement = row.selectFirst("div.text-white.font-bold.truncate");
                if (nameElement == null) continue;
                String champName = nameElement.text().trim();
                
                Element wrElement = row.selectFirst("div.text-accent-orange-500");
                if (wrElement == null) continue;
                String wrText = wrElement.text().replace("% WR", "").replace("%", "").trim();
                
                Element gamesElement = row.selectFirst("div.text-accent-gray-100.text-\\[11px\\]");
                if (gamesElement == null) {
                    Elements possibleGames = row.select("div:containsOwn(games)");
                    gamesElement = possibleGames.isEmpty() ? null : possibleGames.first();
                }
                
                String gamesText = gamesElement != null ? 
                    gamesElement.text().replace("games", "").replace(",", "").trim() : "0";
                
                if (!champName.isEmpty() && !wrText.isEmpty()) {
                    double winRate = Double.parseDouble(wrText);
                    int games = gamesText.isEmpty() ? 0 : Integer.parseInt(gamesText);
                    
                    if (winRate >= 0 && winRate <= 100 && games >= 0) {
                        worstPicks.add(new CounterData(champName, winRate, games));
                    }
                }
            } catch (NumberFormatException e) {
                // Skip invalid data
            }
        }
        
        if (worstPicks.isEmpty()) {
            logger.warn("No valid worst picks data extracted for {}", championName);
        }
        
        return worstPicks;
    }
    
    /**
     * Scrape synergy data using similar structure to worst picks
     */
    private Map<String, Double> scrapeSynergyData(String championName) throws IOException, InterruptedException {
        String url = baseUrl + "/lol/champions/" + championName.toLowerCase() + "/synergy";
        Document doc = fetchDocument(url);
        
        Map<String, Double> synergyData = new HashMap<>();
        
        Elements synergyRows = doc.select("a.flex.items-center[class*=p-\\[12px\\]]");
        
        if (synergyRows.isEmpty()) {
            synergyRows = doc.select("a:has(div.text-white.font-bold.truncate)");
        }
        
        for (Element row : synergyRows) {
            try {
                Element nameElement = row.selectFirst("div.text-white.font-bold.truncate");
                if (nameElement == null) continue;
                String champion = nameElement.text().trim();
                
                Element wrElement = row.selectFirst("div[class*=text-accent-]:containsOwn(WR)");
                if (wrElement == null) continue;
                String wrText = wrElement.text().replace("% WR", "").replace("%", "").trim();
                
                if (!champion.isEmpty() && !wrText.isEmpty()) {
                    double winRate = Double.parseDouble(wrText);
                    if (winRate >= 0 && winRate <= 100) {
                        synergyData.put(champion, winRate);
                    }
                }
            } catch (NumberFormatException e) {
                // Skip invalid data
            }
        }
        
        return synergyData;
    }
    
    /**
     * Scrape tier list data
     */
    private Map<String, Integer> scrapeTierListData(String role) throws IOException, InterruptedException {
        String url = baseUrl + "/lol/tier-list?role=" + role.toLowerCase();
        Document doc = fetchDocument(url);
        
        Map<String, Integer> tierList = new HashMap<>();
        
        Elements championRows = doc.select("a.flex.items-center[class*=p-\\[12px\\]]");
        
        for (Element row : championRows) {
            try {
                Element nameElement = row.selectFirst("div.text-white.font-bold.truncate");
                if (nameElement == null) continue;
                String champion = nameElement.text().trim();
                
                Elements tierElements = row.select("div:matches(^[1-5]$)");
                if (tierElements.isEmpty()) continue;
                
                String tierText = tierElements.first().text().trim();
                if (!champion.isEmpty() && !tierText.isEmpty()) {
                    int tier = Integer.parseInt(tierText);
                    tierList.put(champion, tier);
                }
            } catch (NumberFormatException e) {
                // Skip invalid data
            }
        }
        
        return tierList;
    }
    
    /**
     * Scrape champion stats
     */
    private Map<String, ChampionStats> scrapeChampionStats(String role) throws IOException, InterruptedException {
        String url = baseUrl + "/lol/tier-list?role=" + role.toLowerCase();
        Document doc = fetchDocument(url);
        
        Map<String, ChampionStats> stats = new HashMap<>();
        
        Elements championRows = doc.select("a.flex.items-center[class*=p-\\[12px\\]]");
        
        for (Element row : championRows) {
            try {
                Element nameElement = row.selectFirst("div.text-white.font-bold.truncate");
                if (nameElement == null) continue;
                String champion = nameElement.text().trim();
                
                Element wrElement = row.selectFirst("div[class*=text-accent-]:containsOwn(WR)");
                if (wrElement == null) continue;
                String wrText = wrElement.text().replace("% WR", "").replace("%", "").trim();
                
                Elements tierElements = row.select("div:matches(^[1-5]$)");
                String tierText = tierElements.isEmpty() ? "0" : tierElements.first().text().trim();
                
                if (!champion.isEmpty() && !wrText.isEmpty()) {
                    double winRate = Double.parseDouble(wrText);
                    int tier = tierText.isEmpty() ? 0 : Integer.parseInt(tierText);
                    
                    if (winRate >= 0 && winRate <= 100) {
                        stats.put(champion, new ChampionStats(winRate, tier));
                    }
                }
            } catch (NumberFormatException e) {
                // Skip invalid data
            }
        }
        
        return stats;
    }
    
    /**
     * Clear all caches
     */
    @CacheEvict(value = {"goodMatchups", "synergy", "tierList", "championStats"}, allEntries = true)
    public void clearCache() {
        logger.info("All caches cleared");
    }
    
    /**
     * Clear cache for specific champion
     */
    @CacheEvict(value = {"goodMatchups", "synergy"}, key = "#championName")
    public void clearCacheForChampion(String championName) {
        logger.info("Cache cleared for champion: {}", championName);
    }
    
    /**
     * Clear cache for specific role
     */
    @CacheEvict(value = {"tierList", "championStats"}, key = "#role")
    public void clearCacheForRole(String role) {
        logger.info("Cache cleared for role: {}", role);
    }
}
