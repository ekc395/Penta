package com.penta.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UggDataService {
    
    @Value("${ugg.base-url}")
    private String baseUrl;
    
    @Value("${ugg.timeout}")
    private int timeout;
    
    private final Map<String, Map<String, Double>> synergyCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> matchupCache = new ConcurrentHashMap<>();
    
    /**
     * Get synergy data between champions
     */
    public Map<String, Double> getChampionSynergy(String championName) {
        if (synergyCache.containsKey(championName)) {
            return synergyCache.get(championName);
        }
        
        try {
            Map<String, Double> synergyData = scrapeSynergyData(championName);
            synergyCache.put(championName, synergyData);
            return synergyData;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Get matchup data for a champion
     */
    public Map<String, Double> getChampionMatchups(String championName) {
        if (matchupCache.containsKey(championName)) {
            return matchupCache.get(championName);
        }
        
        try {
            Map<String, Double> matchupData = scrapeMatchupData(championName);
            matchupCache.put(championName, matchupData);
            return matchupData;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Get champion tier list data
     */
    public Map<String, Integer> getChampionTierList(String role) {
        try {
            return scrapeTierListData(role);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Get champion win rates and pick rates
     */
    public Map<String, ChampionStats> getChampionStats(String role) {
        try {
            return scrapeChampionStats(role);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
    
    private Map<String, Double> scrapeSynergyData(String championName) throws IOException {
        Map<String, Double> synergyData = new HashMap<>();
        
        // This would scrape actual synergy data from u.gg
        // For now, returning mock data
        String url = baseUrl + "/lol/champions/" + championName.toLowerCase() + "/synergy";
        
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(timeout)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            
            // Parse synergy data from the page
            Elements synergyElements = doc.select(".synergy-item");
            for (Element element : synergyElements) {
                String champion = element.select(".champion-name").text();
                String winRateText = element.select(".win-rate").text().replace("%", "");
                try {
                    double winRate = Double.parseDouble(winRateText);
                    synergyData.put(champion, winRate);
                } catch (NumberFormatException e) {
                    // Skip invalid data
                }
            }
        } catch (Exception e) {
            // Return mock data if scraping fails
            synergyData = createMockSynergyData(championName);
        }
        
        return synergyData;
    }
    
    private Map<String, Double> scrapeMatchupData(String championName) throws IOException {
        Map<String, Double> matchupData = new HashMap<>();
        
        // This would scrape actual matchup data from u.gg
        // For now, returning mock data
        String url = baseUrl + "/lol/champions/" + championName.toLowerCase() + "/matchups";
        
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(timeout)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            
            // Parse matchup data from the page
            Elements matchupElements = doc.select(".matchup-item");
            for (Element element : matchupElements) {
                String champion = element.select(".champion-name").text();
                String winRateText = element.select(".win-rate").text().replace("%", "");
                try {
                    double winRate = Double.parseDouble(winRateText);
                    matchupData.put(champion, winRate);
                } catch (NumberFormatException e) {
                    // Skip invalid data
                }
            }
        } catch (Exception e) {
            // Return mock data if scraping fails
            matchupData = createMockMatchupData(championName);
        }
        
        return matchupData;
    }
    
    private Map<String, Integer> scrapeTierListData(String role) throws IOException {
        // This would scrape actual tier list data from u.gg
        // For now, returning mock data
        return createMockTierListData(role);
    }
    
    private Map<String, ChampionStats> scrapeChampionStats(String role) throws IOException {
        // This would scrape actual champion stats from u.gg
        // For now, returning mock data
        return createMockChampionStats(role);
    }
    
    // Mock data methods for development
    private Map<String, Double> createMockSynergyData(String championName) {
        Map<String, Double> mockData = new HashMap<>();
        // Add some mock synergy data
        return mockData;
    }
    
    private Map<String, Double> createMockMatchupData(String championName) {
        Map<String, Double> mockData = new HashMap<>();
        // Add some mock matchup data
        return mockData;
    }
    
    private Map<String, Integer> createMockTierListData(String role) {
        Map<String, Integer> mockData = new HashMap<>();
        // Add some mock tier list data
        return mockData;
    }
    
    private Map<String, ChampionStats> createMockChampionStats(String role) {
        Map<String, ChampionStats> mockData = new HashMap<>();
        // Add some mock champion stats
        return mockData;
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
