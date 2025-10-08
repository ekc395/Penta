package com.penta.service;

import com.penta.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataProcessingService {
    
    @Autowired
    private ChampionRepository championRepository;
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private ChampionStatsRepository championStatsRepository;
    
    @Autowired
    private ChampionMatchupRepository championMatchupRepository;
    
    @Autowired
    private ChampionSynergyRepository championSynergyRepository;
    
    /**
     * Process match data and update champion statistics
     */
    @Transactional
    public void processMatchData(Match match) {
        String patch = extractPatchFromVersion(match.getGameVersion());
        String rank = determineRankFromQueue(match.getQueueId());
        
        // Process champion statistics
        processChampionStats(match, patch, rank);
        
        // Process matchup data
        processMatchupData(match, patch, rank);
        
        // Process synergy data
        processSynergyData(match, patch, rank);
    }
    
    /**
     * Calculate and update champion statistics
     */
    private void processChampionStats(Match match, String patch, String rank) {
        Map<Integer, ChampionGameStats> championStats = new HashMap<>();
        
        // Aggregate stats for each champion in the match
        for (MatchParticipant participant : match.getParticipants()) {
            int championId = participant.getChampion().getChampionId();
            String role = participant.getIndividualPosition();
            
            championStats.computeIfAbsent(championId, k -> new ChampionGameStats())
                    .addGame(participant, role);
        }
        
        // Update champion statistics
        for (Map.Entry<Integer, ChampionGameStats> entry : championStats.entrySet()) {
            int championId = entry.getKey();
            ChampionGameStats stats = entry.getValue();
            
            Optional<Champion> championOpt = championRepository.findByChampionId(championId);
            if (championOpt.isPresent()) {
                Champion champion = championOpt.get();
                
                // Update overall champion stats
                updateChampionStats(champion, stats, patch, rank, "ALL");
                
                // Update role-specific stats
                for (String role : stats.getRoles()) {
                    updateChampionStats(champion, stats, patch, rank, role);
                }
            }
        }
    }
    
    /**
     * Calculate and update matchup data
     */
    private void processMatchupData(Match match, String patch, String rank) {
        List<MatchParticipant> participants = match.getParticipants();
        
        // Compare all champion pairs in the match
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                MatchParticipant p1 = participants.get(i);
                MatchParticipant p2 = participants.get(j);
                
                // Only compare champions in the same role
                if (p1.getIndividualPosition().equals(p2.getIndividualPosition()) && 
                    !p1.getTeamId().equals(p2.getTeamId())) {
                    
                    updateMatchupStats(p1, p2, patch, rank);
                }
            }
        }
    }
    
    /**
     * Calculate and update synergy data
     */
    private void processSynergyData(Match match, String patch, String rank) {
        Map<Integer, List<MatchParticipant>> teams = match.getParticipants().stream()
                .collect(Collectors.groupingBy(MatchParticipant::getTeamId));
        
        // Process synergy for each team
        for (List<MatchParticipant> team : teams.values()) {
            for (int i = 0; i < team.size(); i++) {
                for (int j = i + 1; j < team.size(); j++) {
                    MatchParticipant p1 = team.get(i);
                    MatchParticipant p2 = team.get(j);
                    
                    updateSynergyStats(p1, p2, patch, rank);
                }
            }
        }
    }
    
    private void updateChampionStats(Champion champion, ChampionGameStats stats, String patch, String rank, String role) {
        Optional<ChampionStats> existingStatsOpt = championStatsRepository
                .findByChampionAndPatchAndRankAndRole(champion, patch, rank, role);
        
        ChampionStats championStats;
        if (existingStatsOpt.isPresent()) {
            championStats = existingStatsOpt.get();
            championStats.setTotalGames(championStats.getTotalGames() + stats.getTotalGames());
            championStats.setWins(championStats.getWins() + stats.getWins());
            championStats.setLosses(championStats.getLosses() + stats.getLosses());
        } else {
            championStats = new ChampionStats();
            championStats.setChampion(champion);
            championStats.setPatch(patch);
            championStats.setRank(rank);
            championStats.setRole(role);
            championStats.setTotalGames(stats.getTotalGames());
            championStats.setWins(stats.getWins());
            championStats.setLosses(stats.getLosses());
        }
        
        // Recalculate averages
        championStats.setWinRate((double) championStats.getWins() / championStats.getTotalGames() * 100);
        championStats.setAverageKills(stats.getAverageKills());
        championStats.setAverageDeaths(stats.getAverageDeaths());
        championStats.setAverageAssists(stats.getAverageAssists());
        championStats.setAverageCs(stats.getAverageCs());
        championStats.setAverageGold(stats.getAverageGold());
        championStats.setAverageDamage(stats.getAverageDamage());
        championStats.setAverageVisionScore(stats.getAverageVisionScore());
        championStats.setLastUpdated(LocalDateTime.now());
        
        // Calculate tier based on win rate and other factors
        championStats.setTier(calculateTier(championStats));
        
        championStatsRepository.save(championStats);
    }
    
    private void updateMatchupStats(MatchParticipant p1, MatchParticipant p2, String patch, String rank) {
        Champion champion1 = p1.getChampion();
        Champion champion2 = p2.getChampion();
        String role = p1.getIndividualPosition();
        
        Optional<ChampionMatchup> existingMatchupOpt = championMatchupRepository
                .findByChampion1AndChampion2AndPatchAndRankAndRole(champion1, champion2, patch, rank, role);
        
        ChampionMatchup matchup;
        if (existingMatchupOpt.isPresent()) {
            matchup = existingMatchupOpt.get();
        } else {
            matchup = new ChampionMatchup();
            matchup.setChampion1(champion1);
            matchup.setChampion2(champion2);
            matchup.setPatch(patch);
            matchup.setRank(rank);
            matchup.setRole(role);
            matchup.setTotalGames(0);
            matchup.setChampion1Wins(0);
            matchup.setChampion2Wins(0);
        }
        
        matchup.setTotalGames(matchup.getTotalGames() + 1);
        
        if (p1.getWon()) {
            matchup.setChampion1Wins(matchup.getChampion1Wins() + 1);
        } else {
            matchup.setChampion2Wins(matchup.getChampion2Wins() + 1);
        }
        
        matchup.setChampion1WinRate((double) matchup.getChampion1Wins() / matchup.getTotalGames() * 100);
        matchup.setChampion2WinRate((double) matchup.getChampion2Wins() / matchup.getTotalGames() * 100);
        
        // Calculate matchup score (-1 to 1)
        double winRateDiff = matchup.getChampion1WinRate() - 50.0;
        matchup.setMatchupScore(winRateDiff / 50.0); // Normalize to -1 to 1
        
        matchup.setLastUpdated(LocalDateTime.now());
        championMatchupRepository.save(matchup);
    }
    
    private void updateSynergyStats(MatchParticipant p1, MatchParticipant p2, String patch, String rank) {
        Champion champion1 = p1.getChampion();
        Champion champion2 = p2.getChampion();
        
        Optional<ChampionSynergy> existingSynergyOpt = championSynergyRepository
                .findByChampion1AndChampion2AndPatchAndRank(champion1, champion2, patch, rank);
        
        ChampionSynergy synergy;
        if (existingSynergyOpt.isPresent()) {
            synergy = existingSynergyOpt.get();
        } else {
            synergy = new ChampionSynergy();
            synergy.setChampion1(champion1);
            synergy.setChampion2(champion2);
            synergy.setPatch(patch);
            synergy.setRank(rank);
            synergy.setTotalGames(0);
            synergy.setWins(0);
            synergy.setLosses(0);
            synergy.setSynergyType("TEAM");
        }
        
        synergy.setTotalGames(synergy.getTotalGames() + 1);
        
        if (p1.getWon()) {
            synergy.setWins(synergy.getWins() + 1);
        } else {
            synergy.setLosses(synergy.getLosses() + 1);
        }
        
        synergy.setWinRate((double) synergy.getWins() / synergy.getTotalGames() * 100);
        
        // Calculate synergy score (0 to 1)
        double baseWinRate = 50.0; // Expected win rate
        double synergyBonus = synergy.getWinRate() - baseWinRate;
        synergy.setSynergyScore(Math.max(0, Math.min(1, synergyBonus / 50.0 + 0.5))); // Normalize to 0-1
        
        synergy.setLastUpdated(LocalDateTime.now());
        championSynergyRepository.save(synergy);
    }
    
    private String extractPatchFromVersion(String gameVersion) {
        if (gameVersion == null || gameVersion.isEmpty()) {
            return "unknown";
        }
        // Extract patch from version like "13.24.1" -> "13.24"
        String[] parts = gameVersion.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return gameVersion;
    }
    
    private String determineRankFromQueue(int queueId) {
        // Map queue IDs to rank categories
        return switch (queueId) {
            case 420 -> "DIAMOND_PLUS"; // Ranked Solo/Duo
            case 440 -> "DIAMOND_PLUS"; // Ranked Flex
            case 400 -> "ALL"; // Draft Pick
            case 430 -> "ALL"; // Blind Pick
            default -> "ALL";
        };
    }
    
    private int calculateTier(ChampionStats stats) {
        // Simple tier calculation based on win rate
        double winRate = stats.getWinRate();
        if (winRate >= 55) return 5; // S tier
        if (winRate >= 52) return 4; // A tier
        if (winRate >= 49) return 3; // B tier
        if (winRate >= 46) return 2; // C tier
        return 1; // D tier
    }
    
    // Helper class for aggregating champion game statistics
    private static class ChampionGameStats {
        private int totalGames = 0;
        private int wins = 0;
        private int losses = 0;
        private double totalKills = 0;
        private double totalDeaths = 0;
        private double totalAssists = 0;
        private double totalCs = 0;
        private double totalGold = 0;
        private double totalDamage = 0;
        private double totalVisionScore = 0;
        private Set<String> roles = new HashSet<>();
        
        public void addGame(MatchParticipant participant, String role) {
            totalGames++;
            if (participant.getWon()) {
                wins++;
            } else {
                losses++;
            }
            
            totalKills += participant.getKills();
            totalDeaths += participant.getDeaths();
            totalAssists += participant.getAssists();
            totalCs += participant.getCs();
            totalGold += participant.getGoldEarned();
            totalDamage += participant.getDamageDealt();
            totalVisionScore += participant.getVisionScore();
            roles.add(role);
        }
        
        // Getters
        public int getTotalGames() { return totalGames; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public double getAverageKills() { return totalGames > 0 ? totalKills / totalGames : 0; }
        public double getAverageDeaths() { return totalGames > 0 ? totalDeaths / totalGames : 0; }
        public double getAverageAssists() { return totalGames > 0 ? totalAssists / totalGames : 0; }
        public double getAverageCs() { return totalGames > 0 ? totalCs / totalGames : 0; }
        public double getAverageGold() { return totalGames > 0 ? totalGold / totalGames : 0; }
        public double getAverageDamage() { return totalGames > 0 ? totalDamage / totalGames : 0; }
        public double getAverageVisionScore() { return totalGames > 0 ? totalVisionScore / totalGames : 0; }
        public Set<String> getRoles() { return roles; }
    }
}
