package com.penta.service;

import com.penta.model.*;
import com.penta.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;
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

    @Autowired
    private PlayerChampionRepository playerChampionRepository;

    @Autowired
    private PlayerMatchRepository playerMatchRepository;    
        
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
                // Create PlayerMatch records for this player
                createPlayerMatchRecords(player, matchId);
            }
            
            // Create/update PlayerChampion records
            updatePlayerChampionStats(player);
            
            // Update player's last updated timestamp
            player.setLastUpdated(LocalDateTime.now());
            playerRepository.save(player);
            
        } catch (Exception e) {
            throw new RuntimeException("Error collecting player data: " + e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private void createPlayerMatchRecords(Player player, String matchId) {
        Optional<Match> matchOpt = matchRepository.findByMatchId(matchId);
        if (matchOpt.isEmpty()) return;
        
        Match match = matchOpt.get();
        
        // Find the participant that matches this player
        match.getParticipants().stream()
            .filter(p -> p.getPuuid().equals(player.getPuuid()))
            .findFirst()
            .ifPresent(participant -> {
                PlayerMatch pm = new PlayerMatch();
                pm.setPlayer(player);
                pm.setChampion(participant.getChampion());
                pm.setMatch(match);  // ADD THIS - links to full match with all participants
                pm.setMatchId(matchId);
                pm.setGameMode(match.getGameMode());
                pm.setGameType(match.getGameType());
                pm.setGameStartTime(match.getGameStartTime());
                pm.setGameDuration(match.getGameDuration());
                pm.setKills(participant.getKills());
                pm.setDeaths(participant.getDeaths());
                pm.setAssists(participant.getAssists());
                pm.setCs(participant.getCs());
                pm.setWon(participant.getWon());
                pm.setLane(participant.getIndividualPosition());
                pm.setRole(participant.getTeamPosition());
                pm.setTeamId(participant.getTeamId());
                // ADD THESE - damage, gold, vision stats
                pm.setDamageDealt(participant.getDamageDealt());
                pm.setDamageTaken(participant.getDamageTaken());
                pm.setGoldEarned(participant.getGoldEarned());
                pm.setVisionScore(participant.getVisionScore());
                pm.setWardsPlaced(participant.getWardsPlaced());
                pm.setWardsKilled(participant.getWardsKilled());
                
                playerMatchRepository.save(pm);
            });
    }
    private void updatePlayerChampionStats(Player player) {
        // Get all matches for this player
        List<PlayerMatch> matches = playerMatchRepository.findByPlayerOrderByGameStartTimeDesc(player);
        
        // Group by champion and calculate stats
        Map<Champion, List<PlayerMatch>> matchesByChampion = matches.stream()
            .collect(Collectors.groupingBy(PlayerMatch::getChampion));
        
        matchesByChampion.forEach((champion, championMatches) -> {
            PlayerChampion pc = playerChampionRepository
                .findByPlayerAndChampion(player, champion)
                .orElse(new PlayerChampion());
            
            pc.setPlayer(player);
            pc.setChampion(champion);
            pc.setGamesPlayed(championMatches.size());
            pc.setWins((int) championMatches.stream().filter(PlayerMatch::getWon).count());
            pc.setLosses(championMatches.size() - pc.getWins());
            pc.setWinRate(pc.getGamesPlayed() > 0 ? (double) pc.getWins() / pc.getGamesPlayed() * 100 : 0.0);
            pc.setAverageKills(championMatches.stream().mapToInt(PlayerMatch::getKills).average().orElse(0.0));
            pc.setAverageDeaths(championMatches.stream().mapToInt(PlayerMatch::getDeaths).average().orElse(0.0));
            pc.setAverageAssists(championMatches.stream().mapToInt(PlayerMatch::getAssists).average().orElse(0.0));
            pc.setAverageCs(championMatches.stream().mapToInt(PlayerMatch::getCs).average().orElse(0.0));
            pc.setLastPlayed(championMatches.get(0).getGameStartTime());
            
            playerChampionRepository.save(pc);
        });
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
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<Match> recentMatches = matchRepository.findRecentMatches(since);
            
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
        status.setTotalMatches(matchRepository.count());
        status.setTotalChampions(championRepository.count());
        status.setTotalPlayers(playerRepository.count());
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