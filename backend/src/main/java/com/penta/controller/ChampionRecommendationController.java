package com.penta.controller;

import com.penta.dto.ChampionRecommendationDto;
import com.penta.service.ChampionRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend to access
public class ChampionRecommendationController {
    
    @Autowired
    private ChampionRecommendationService recommendationService;
    
    /**
     * Get champion recommendations for a player
     */
    @GetMapping("/player/{summonerName}")
    public ResponseEntity<List<ChampionRecommendationDto>> getRecommendations(
            @PathVariable String summonerName,
            @RequestParam String region,
            @RequestParam(required = false) List<String> teamChampions,
            @RequestParam(required = false) List<String> opponentChampions,
            @RequestParam(required = false) String preferredRole) {
        
        try {
            List<ChampionRecommendationDto> recommendations = recommendationService.getRecommendations(
                    summonerName, 
                    region, 
                    teamChampions != null ? teamChampions : List.of(),
                    opponentChampions != null ? opponentChampions : List.of(),
                    preferredRole != null ? preferredRole : "MID"
            );
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get champion recommendations for team composition
     */
    @PostMapping("/team")
    public ResponseEntity<List<ChampionRecommendationDto>> getTeamRecommendations(
            @RequestBody TeamRecommendationRequest request) {
        
        try {
            List<ChampionRecommendationDto> recommendations = recommendationService.getRecommendations(
                    request.getSummonerName(),
                    request.getRegion(),
                    request.getTeamChampions(),
                    request.getOpponentChampions(),
                    request.getPreferredRole()
            );
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get champion recommendations for counter-picking
     */
    @GetMapping("/counter/{championName}")
    public ResponseEntity<List<ChampionRecommendationDto>> getCounterRecommendations(
            @PathVariable String championName,
            @RequestParam String region,
            @RequestParam(required = false) String preferredRole) {
        
        try {
            // This would implement counter-pick logic
            // For now, returning empty list
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Request DTO for team recommendations
     */
    public static class TeamRecommendationRequest {
        private String summonerName;
        private String region;
        private List<String> teamChampions;
        private List<String> opponentChampions;
        private String preferredRole;
        
        // Getters and setters
        public String getSummonerName() { return summonerName; }
        public void setSummonerName(String summonerName) { this.summonerName = summonerName; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public List<String> getTeamChampions() { return teamChampions; }
        public void setTeamChampions(List<String> teamChampions) { this.teamChampions = teamChampions; }
        
        public List<String> getOpponentChampions() { return opponentChampions; }
        public void setOpponentChampions(List<String> opponentChampions) { this.opponentChampions = opponentChampions; }
        
        public String getPreferredRole() { return preferredRole; }
        public void setPreferredRole(String preferredRole) { this.preferredRole = preferredRole; }
    }
}
