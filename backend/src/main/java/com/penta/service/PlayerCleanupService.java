package com.penta.service;

import com.penta.model.Player;
import com.penta.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlayerCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerCleanupService.class);
    
    @Autowired
    private PlayerRepository playerRepository;
    
    /**
     * Run cleanup when application starts
     */
    @PostConstruct
    public void cleanupOnStartup() {
        logger.info("Running player cleanup on application startup...");
        try {
            int removedCount = cleanupStalePlayersManual(7);
            logger.info("Startup cleanup completed. Removed {} stale players", removedCount);
        } catch (Exception e) {
            logger.error("Error during startup cleanup: {}", e.getMessage());
        }
    }
    
    /**
     * Cleanup players that haven't been accessed in 7 days
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupStalePlayers() {
        logger.info("Starting cleanup of stale players...");
        
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Player> stalePlayers = playerRepository.findStalePlayersBefore(threshold);
        
        if (stalePlayers.isEmpty()) {
            logger.info("No stale players found");
            return;
        }
        
        logger.info("Found {} stale players to remove", stalePlayers.size());
        
        for (Player player : stalePlayers) {
            try {
                playerRepository.delete(player);
                logger.debug("Deleted stale player: {}", player.getSummonerName());
            } catch (Exception e) {
                logger.error("Error deleting player {}: {}", player.getSummonerName(), e.getMessage());
            }
        }
        
        logger.info("Cleanup completed. Removed {} stale players", stalePlayers.size());
    }
    
    /**
     * Manual cleanup method that can be called via endpoint
     */
    @Transactional
    public int cleanupStalePlayersManual(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        List<Player> stalePlayers = playerRepository.findStalePlayersBefore(threshold);
        
        int count = 0;
        for (Player player : stalePlayers) {
            try {
                playerRepository.delete(player);
                count++;
            } catch (Exception e) {
                logger.error("Error deleting player {}: {}", player.getSummonerName(), e.getMessage());
            }
        }
        
        return count;
    }
}

