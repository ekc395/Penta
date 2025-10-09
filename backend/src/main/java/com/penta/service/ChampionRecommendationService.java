package com.penta.service;

import com.penta.dto.ChampionRecommendationDto;
import com.penta.dto.ChampionDto;
import com.penta.model.Champion;
import com.penta.model.Player;
import com.penta.model.PlayerChampion;
import com.penta.service.UggDataService.CounterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChampionRecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChampionRecommendationService.class);
    
    @Autowired
    private UggDataService uggDataService;
    
    @Autowired
    private LeagueApiService leagueApiService;
    
    /**
     * Get champion recommendations for a player based on team composition and opponent picks
     */
    public List<ChampionRecommendationDto> getRecommendations(
            String summonerName, 
            String region, 
            List<String> teamChampions, 
            List<String> opponentChampions, 
            String preferredRole) {
        
        // Get player data
        Optional<Player> playerOpt = leagueApiService.getPlayerBySummonerName(summonerName, region);
        if (playerOpt.isEmpty()) {
            logger.warn("Player not found: {} in region {}", summonerName, region);
            return Collections.emptyList();
        }
        
        Player player = playerOpt.get();
        
        // Get all available champions for the role
        List<Champion> availableChampions = getChampionsForRole(preferredRole);
        
        // Calculate recommendations
        List<ChampionRecommendationDto> recommendations = availableChampions.stream()
                .map(champion -> calculateRecommendation(champion, player, teamChampions, opponentChampions))
                .sorted((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                .limit(10)
                .collect(Collectors.toList());
        
        return recommendations;
    }
    
    /**
     * Calculate recommendation score for a specific champion
     */
    private ChampionRecommendationDto calculateRecommendation(
            Champion champion, 
            Player player, 
            List<String> teamChampions, 
            List<String> opponentChampions) {
        
        ChampionRecommendationDto recommendation = new ChampionRecommendationDto();
        recommendation.setChampion(convertToDto(champion));
        
        // Calculate different factors
        double playerComfortScore = calculatePlayerComfort(champion, player);
        double teamSynergyScore = calculateTeamSynergy(champion, teamChampions);
        double opponentMatchupScore = calculateOpponentMatchup(champion, opponentChampions);
        double metaScore = calculateMetaScore(champion);
        
        // Weighted final score
        double finalScore = (playerComfortScore * 0.4) + 
                           (teamSynergyScore * 0.3) + 
                           (opponentMatchupScore * 0.2) + 
                           (metaScore * 0.1);
        
        recommendation.setRecommendationScore(finalScore);
        recommendation.setReason(generateRecommendationReason(playerComfortScore, teamSynergyScore, opponentMatchupScore, metaScore));
        
        return recommendation;
    }
    
    /**
     * Calculate how comfortable the player is with this champion
     */
    private double calculatePlayerComfort(Champion champion, Player player) {
        if (player.getRecentChampions() == null) {
            return 0.5; // Default comfort for unknown champions
        }
        
        Optional<PlayerChampion> playerChampionOpt = player.getRecentChampions().stream()
                .filter(pc -> pc.getChampion().getChampionId().equals(champion.getChampionId()))
                .findFirst();
        
        if (playerChampionOpt.isEmpty()) {
            return 0.3; // Lower comfort for unplayed champions
        }
        
        PlayerChampion playerChampion = playerChampionOpt.get();
        
        // Calculate comfort based on games played, win rate, and mastery
        double gamesWeight = Math.min(playerChampion.getGamesPlayed() / 50.0, 1.0); // Cap at 50 games
        double winRateWeight = playerChampion.getWinRate() / 100.0;
        double masteryWeight = Math.min(playerChampion.getMasteryLevel() / 7.0, 1.0); // Cap at mastery 7
        
        return (gamesWeight * 0.4) + (winRateWeight * 0.4) + (masteryWeight * 0.2);
    }
    
    /**
     * Calculate team synergy score
     */
    private double calculateTeamSynergy(Champion champion, List<String> teamChampions) {
        if (teamChampions == null || teamChampions.isEmpty()) {
            return 0.5; // Neutral score if no team data
        }
        
        Optional<Map<String, Double>> synergyDataOpt = uggDataService.getChampionSynergy(champion.getName());
        
        if (synergyDataOpt.isEmpty()) {
            return 0.5; // Neutral score if data fetch failed
        }
        
        Map<String, Double> synergyData = synergyDataOpt.get();
        double totalSynergy = 0.0;
        int validChampions = 0;
        
        for (String teamChampion : teamChampions) {
            if (synergyData.containsKey(teamChampion)) {
                totalSynergy += synergyData.get(teamChampion);
                validChampions++;
            }
        }
        
        if (validChampions == 0) {
            return 0.5; // Neutral score if no synergy data
        }
        
        return (totalSynergy / validChampions) / 100.0; // Convert percentage to 0-1 scale
    }
    
    /**
     * Calculate opponent matchup score
     */
    private double calculateOpponentMatchup(Champion champion, List<String> opponentChampions) {
        if (opponentChampions == null || opponentChampions.isEmpty()) {
            return 0.5; // Neutral score if no opponent data
        }
        
        Optional<List<CounterData>> matchupDataOpt = uggDataService.getGoodMatchups(champion.getName());
        
        if (matchupDataOpt.isEmpty()) {
            return 0.5; // Neutral score if data fetch failed
        }
        
        List<CounterData> matchupData = matchupDataOpt.get();
        
        // Create a map for easier lookup
        Map<String, Double> matchupMap = matchupData.stream()
                .collect(Collectors.toMap(
                    CounterData::getChampionName,
                    CounterData::getWinRate,
                    (existing, replacement) -> existing // Handle duplicates by keeping first
                ));
        
        double totalMatchup = 0.0;
        int validChampions = 0;
        
        for (String opponentChampion : opponentChampions) {
            if (matchupMap.containsKey(opponentChampion)) {
                totalMatchup += matchupMap.get(opponentChampion);
                validChampions++;
            }
        }
        
        if (validChampions == 0) {
            return 0.5; // Neutral score if no matchup data
        }
        
        return (totalMatchup / validChampions) / 100.0; // Convert percentage to 0-1 scale
    }
    
    /**
     * Calculate meta score based on current champion performance
     */
    private double calculateMetaScore(Champion champion) {
        // This would use current meta data from u.gg
        // For now, using a simple tier-based score
        if (champion.getTier() == null) {
            return 0.5;
        }
        
        // Convert tier to score (S=5, A=4, B=3, C=2, D=1)
        return champion.getTier() / 5.0;
    }
    
    /**
     * Generate human-readable recommendation reason
     */
    private String generateRecommendationReason(double playerComfort, double teamSynergy, double opponentMatchup, double metaScore) {
        List<String> reasons = new ArrayList<>();
        
        if (playerComfort > 0.7) {
            reasons.add("You have high experience with this champion");
        } else if (playerComfort < 0.3) {
            reasons.add("You have limited experience with this champion");
        }
        
        if (teamSynergy > 0.6) {
            reasons.add("Great synergy with your team composition");
        } else if (teamSynergy < 0.4) {
            reasons.add("Poor synergy with your team composition");
        }
        
        if (opponentMatchup > 0.6) {
            reasons.add("Strong against opponent champions");
        } else if (opponentMatchup < 0.4) {
            reasons.add("Weak against opponent champions");
        }
        
        if (metaScore > 0.7) {
            reasons.add("Currently strong in the meta");
        }
        
        return String.join(". ", reasons);
    }
    
    /**
     * Get champions for a specific role
     */
    private List<Champion> getChampionsForRole(String role) {
        // This would query the database for champions in the specified role
        // For now, returning mock data
        return new ArrayList<>();
    }
    
    /**
     * Convert Champion entity to DTO
     */
    private ChampionDto convertToDto(Champion champion) {
        ChampionDto dto = new ChampionDto();
        dto.setId(champion.getId());
        dto.setChampionId(champion.getChampionId());
        dto.setName(champion.getName());
        dto.setTitle(champion.getTitle());
        dto.setRole(champion.getRole());
        dto.setLane(champion.getLane());
        dto.setImageUrl(champion.getImageUrl());
        dto.setSplashUrl(champion.getSplashUrl());
        dto.setWinRate(champion.getWinRate());
        dto.setPickRate(champion.getPickRate());
        dto.setBanRate(champion.getBanRate());
        dto.setTier(champion.getTier());
        dto.setTags(champion.getTags());
        return dto;
    }
}