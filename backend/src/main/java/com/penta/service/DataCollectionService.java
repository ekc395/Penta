package com.penta.service;

import com.penta.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class DataCollectionService {
    
    @Autowired
    private RiotApiService riotApiService;
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    @Autowired
    private ChampionRepository championRepository;
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    /**
     * Collect and process data for a specific player
     */
    @Async
    @Transactional
    public CompletableFuture<Void> collectPlayerData(String summonerName, String region, int matchCount) {
        try {
            // Get player information
            Optional<Player> playerOpt = riotApiService.getPlayerBySummonerName(summonerName, region);
            if (playerOpt.isEmpty()) {
                throw new RuntimeException("Player not found: " + summonerName);
            }
            
            Player player = playerOpt.get();
            playerRepository.save(player);
            
            // Get match history
            List<String> matchIds = riotApiService.getMatchHistory(player.getPuuid(), region, matchCount);
            
            // Process each match
            for (String matchId : matchIds) {
                processMatch(matchId, region);
            }
            
            // Update player's last updated timestamp
            player.setLastUpdated(LocalDateTime.now());
            playerRepository.save(player);
            
        } catch (Exception e) {
            throw new RuntimeException("Error collecting player data: " + e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Collect and process data for multiple players
     */
    @Async
    @Transactional
    public CompletableFuture<Void> collectMultiplePlayersData(List<String> summonerNames, String region, int matchCount) {
        for (String summonerName : summonerNames) {
            try {
                collectPlayerData(summonerName, region, matchCount).get();
            } catch (Exception e) {
                System.err.println("Error processing player " + summonerName + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Process a single match
     */
    @Transactional
    public void processMatch(String matchId, String region) {
        try {
            // Check if match already exists
            Optional<Match> existingMatch = matchRepository.findByMatchId(matchId);
            if (existingMatch.isPresent()) {
                return; // Match already processed
            }
            
            // Get match details from Riot API
            Optional<Match> matchOpt = riotApiService.getMatchDetails(matchId, region);
            if (matchOpt.isEmpty()) {
                return; // Match not found or invalid
            }
            
            Match match = matchOpt.get();
            
            // Save match and participants
            matchRepository.save(match);
            
            // Process match data for statistics
            dataProcessingService.processMatchData(match);
            
        } catch (Exception e) {
            throw new RuntimeException("Error processing match " + matchId + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize champion data from Riot API
     */
    @Transactional
    public void initializeChampionData() {
        try {
            List<Champion> champions = riotApiService.getAllChampions();
            
            for (Champion champion : champions) {
                Optional<Champion> existingChampion = championRepository.findByChampionId(champion.getChampionId());
                if (existingChampion.isEmpty()) {
                    championRepository.save(champion);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error initializing champion data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Collect data for high-elo players to build comprehensive dataset
     */
    @Async
    @Transactional
    public CompletableFuture<Void> collectHighEloData(String region, int playersPerTier, int matchesPerPlayer) {
        try {
            // This would typically involve:
            // 1. Getting leaderboard data from Riot API
            // 2. Collecting match data for top players
            // 3. Processing the data for statistics
            
            // For now, this is a placeholder for the high-elo data collection
            System.out.println("Starting high-elo data collection for region: " + region);
            
            // TODO: Implement leaderboard scraping and data collection
            
        } catch (Exception e) {
            throw new RuntimeException("Error collecting high-elo data: " + e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Update champion statistics from recent matches
     */
    @Transactional
    public void updateChampionStatistics(String patch, String rank) {
        try {
            // Get recent matches for the specified patch and rank
            LocalDateTime since = LocalDateTime.now().minusDays(7); // Last 7 days
            List<Match> recentMatches = matchRepository.findRecentMatches(since);
            
            // Process each match to update statistics
            for (Match match : recentMatches) {
                if (match.getGameVersion().startsWith(patch)) {
                    dataProcessingService.processMatchData(match);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error updating champion statistics: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get data collection status
     */
    public DataCollectionStatus getDataCollectionStatus() {
        DataCollectionStatus status = new DataCollectionStatus();
        
        // Count total matches processed
        status.setTotalMatches(matchRepository.count());
        
        // Count total champions
        status.setTotalChampions(championRepository.count());
        
        // Count total players
        status.setTotalPlayers(playerRepository.count());
        
        // Get recent activity
        LocalDateTime last24Hours = LocalDateTime.now().minusDays(1);
        status.setMatchesLast24Hours(matchRepository.countMatchesSince(last24Hours));
        
        return status;
    }
    
    /**
     * Data collection status DTO
     */
    public static class DataCollectionStatus {
        private long totalMatches;
        private long totalChampions;
        private long totalPlayers;
        private long matchesLast24Hours;
        
        // Getters and setters
        public long getTotalMatches() { return totalMatches; }
        public void setTotalMatches(long totalMatches) { this.totalMatches = totalMatches; }
        public long getTotalChampions() { return totalChampions; }
        public void setTotalChampions(long totalChampions) { this.totalChampions = totalChampions; }
        public long getTotalPlayers() { return totalPlayers; }
        public void setTotalPlayers(long totalPlayers) { this.totalPlayers = totalPlayers; }
        public long getMatchesLast24Hours() { return matchesLast24Hours; }
        public void setMatchesLast24Hours(long matchesLast24Hours) { this.matchesLast24Hours = matchesLast24Hours; }
    }
}
