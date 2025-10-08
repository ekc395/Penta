package com.penta.controller;

import com.penta.service.DataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
@CrossOrigin(origins = "http://localhost:5173")
public class DataCollectionController {
    
    @Autowired
    private DataCollectionService dataCollectionService;
    
    /**
     * Initialize champion data from Riot API
     */
    @PostMapping("/champions/initialize")
    public ResponseEntity<String> initializeChampions() {
        try {
            dataCollectionService.initializeChampionData();
            return ResponseEntity.ok("Champion data initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error initializing champions: " + e.getMessage());
        }
    }
    
    /**
     * Collect data for a specific player
     */
    @PostMapping("/player/{summonerName}")
    public ResponseEntity<String> collectPlayerData(
            @PathVariable String summonerName,
            @RequestParam String region,
            @RequestParam(defaultValue = "20") int matchCount) {
        try {
            dataCollectionService.collectPlayerData(summonerName, region, matchCount);
            return ResponseEntity.ok("Player data collection started for " + summonerName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error collecting player data: " + e.getMessage());
        }
    }
    
    /**
     * Collect data for multiple players
     */
    @PostMapping("/players")
    public ResponseEntity<String> collectMultiplePlayersData(
            @RequestBody CollectPlayersRequest request) {
        try {
            dataCollectionService.collectMultiplePlayersData(
                    request.getSummonerNames(), 
                    request.getRegion(), 
                    request.getMatchCount()
            );
            return ResponseEntity.ok("Data collection started for " + request.getSummonerNames().size() + " players");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error collecting players data: " + e.getMessage());
        }
    }
    
    /**
     * Process a specific match
     */
    @PostMapping("/match/{matchId}")
    public ResponseEntity<String> processMatch(
            @PathVariable String matchId,
            @RequestParam String region) {
        try {
            dataCollectionService.processMatch(matchId, region);
            return ResponseEntity.ok("Match processed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing match: " + e.getMessage());
        }
    }
    
    /**
     * Update champion statistics
     */
    @PostMapping("/statistics/update")
    public ResponseEntity<String> updateChampionStatistics(
            @RequestParam String patch,
            @RequestParam String rank) {
        try {
            dataCollectionService.updateChampionStatistics(patch, rank);
            return ResponseEntity.ok("Champion statistics updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating statistics: " + e.getMessage());
        }
    }
    
    /**
     * Get data collection status
     */
    @GetMapping("/status")
    public ResponseEntity<DataCollectionService.DataCollectionStatus> getDataCollectionStatus() {
        try {
            DataCollectionService.DataCollectionStatus status = dataCollectionService.getDataCollectionStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Start high-elo data collection
     */
    @PostMapping("/high-elo")
    public ResponseEntity<String> collectHighEloData(
            @RequestParam String region,
            @RequestParam(defaultValue = "50") int playersPerTier,
            @RequestParam(defaultValue = "30") int matchesPerPlayer) {
        try {
            dataCollectionService.collectHighEloData(region, playersPerTier, matchesPerPlayer);
            return ResponseEntity.ok("High-elo data collection started for region: " + region);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error starting high-elo data collection: " + e.getMessage());
        }
    }
    
    /**
     * Request DTO for collecting multiple players data
     */
    public static class CollectPlayersRequest {
        private List<String> summonerNames;
        private String region;
        private int matchCount;
        
        // Getters and setters
        public List<String> getSummonerNames() { return summonerNames; }
        public void setSummonerNames(List<String> summonerNames) { this.summonerNames = summonerNames; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public int getMatchCount() { return matchCount; }
        public void setMatchCount(int matchCount) { this.matchCount = matchCount; }
    }
}
