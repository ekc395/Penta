package com.penta.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.penta.model.Player;
import com.penta.repository.PlayerRepository;
import com.penta.service.DataCollectionService;
import com.penta.service.PlayerCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.penta.dto.SummonerProfileDto;
import com.penta.service.RiotApiService;
import java.util.List;

@RestController
@RequestMapping("/data")
@CrossOrigin(origins = "http://localhost:5173")
public class DataCollectionController {
    
    @Autowired
    private DataCollectionService dataCollectionService;

    @Autowired
    private RiotApiService riotApiService;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private PlayerCleanupService playerCleanupService;
    
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
     * Get summoner profile - auto-collects data if needed
     */
    @GetMapping("/player/profile")
    public ResponseEntity<?> getSummonerProfile(
            @RequestParam String summonerName,
            @RequestParam String region) {
        try {
            // Check if player exists
            Optional<Player> playerOpt = playerRepository.findBySummonerName(summonerName);
            
            if (playerOpt.isEmpty()) {
                // Player doesn't exist - trigger collection
                dataCollectionService.collectPlayerData(summonerName, region, 20); 
                
                Map<String, String> response = new HashMap<>();
                response.put("status", "collecting");
                response.put("message", "Player data is being collected. This may take a few moments.");
                return ResponseEntity.accepted().body(response);
            }
            
            Player player = playerOpt.get();
            
            // Check if data is stale (older than 1 hour)
            if (player.getLastUpdated() == null || 
                player.getLastUpdated().isBefore(LocalDateTime.now().minusHours(1))) {
                // Trigger background update
                dataCollectionService.collectPlayerData(summonerName, region, 20); 
            }
            
            // Return existing profile data
            Optional<SummonerProfileDto> profileOpt = riotApiService.getSummonerProfile(summonerName, region);
            return profileOpt.map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
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
        
        public List<String> getSummonerNames() { return summonerNames; }
        public void setSummonerNames(List<String> summonerNames) { this.summonerNames = summonerNames; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public int getMatchCount() { return matchCount; }
        public void setMatchCount(int matchCount) { this.matchCount = matchCount; }
    }
    
    /**
     * Manually cleanup stale players
     */
    @PostMapping("/cleanup/players")
    public ResponseEntity<Map<String, Object>> cleanupStalePlayers(
            @RequestParam(defaultValue = "7") int daysThreshold) {
        try {
            int removedCount = playerCleanupService.cleanupStalePlayersManual(daysThreshold);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("removedCount", removedCount);
            response.put("message", String.format("Removed %d stale players older than %d days", removedCount, daysThreshold));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}