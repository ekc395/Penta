package com.penta.controller;

import com.penta.model.Champion;
import com.penta.dto.PlayerDto;
import com.penta.dto.ChampionDto;
import com.penta.dto.PlayerChampionDto;
import com.penta.dto.PlayerMatchDto;
import com.penta.dto.PlayerSearchDto;
import com.penta.model.Player;
import com.penta.model.PlayerChampion;
import com.penta.model.PlayerMatch;
import com.penta.repository.PlayerRepository;
import com.penta.repository.PlayerChampionRepository;
import com.penta.repository.PlayerMatchRepository;
import com.penta.service.RiotApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/players")
@CrossOrigin(origins = "http://localhost:5173")
public class PlayerController {
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private PlayerChampionRepository playerChampionRepository;
    
    @Autowired
    private PlayerMatchRepository playerMatchRepository;
    
    @Autowired
    private RiotApiService riotApiService;
    
    @GetMapping("/{summonerName}")
    public ResponseEntity<PlayerDto> getPlayer(
            @PathVariable String summonerName,
            @RequestParam String region) {
        Optional<Player> playerOpt = playerRepository.findBySummonerName(summonerName);
        
        if (playerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Player player = playerOpt.get();
        PlayerDto dto = convertToDto(player);
        
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<PlayerSearchDto>> searchPlayers(
            @RequestParam String query,
            @RequestParam String region,
            @RequestParam(defaultValue = "5") int limit) {
        
        if (query.length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        
        List<PlayerSearchDto> suggestions = new ArrayList<>();
        
        // First, search in database for cached players
        List<Player> cachedPlayers = playerRepository.findAll().stream()
            .filter(p -> p.getRegion().equalsIgnoreCase(region))
            .filter(p -> p.getSummonerName().toLowerCase().contains(query.toLowerCase()))
            .limit(limit)
            .collect(Collectors.toList());
        
        // Update lastAccessed for cached players and add to suggestions
        for (Player player : cachedPlayers) {
            player.setLastAccessed(LocalDateTime.now());
            playerRepository.save(player);
            suggestions.add(convertToPlayerSearchDto(player));
        }
        
        // If we have enough suggestions from cache, return them
        if (suggestions.size() >= limit) {
            return ResponseEntity.ok(suggestions);
        }
        
        // Check if query already contains a tag (e.g., "player#TAG")
        if (query.contains("#")) {
            try {
                Player player = fetchAndCachePlayer(query, region);
                if (player != null) {
                    boolean isDuplicate = suggestions.stream()
                        .anyMatch(s -> s.getSummonerName().equalsIgnoreCase(player.getSummonerName()));
                    if (!isDuplicate) {
                        suggestions.add(convertToPlayerSearchDto(player));
                    }
                }
            } catch (Exception e) {
                // Continue to try tag patterns
            }
        }
        
        if (suggestions.size() >= limit) {
            return ResponseEntity.ok(suggestions);
        }
        
        return ResponseEntity.ok(suggestions);
    }
    
    private Player fetchAndCachePlayer(String summonerName, String region) {
        // Check if player already exists in database
        Optional<Player> existingPlayerOpt = playerRepository.findBySummonerName(summonerName);
        if (existingPlayerOpt.isPresent()) {
            Player existingPlayer = existingPlayerOpt.get();
            existingPlayer.setLastAccessed(LocalDateTime.now());
            return playerRepository.save(existingPlayer);
        }
        
        // Fetch from Riot API
        Optional<Player> playerOpt = riotApiService.getPlayerBySummonerName(summonerName, region);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setLastAccessed(LocalDateTime.now());
            player.setLastUpdated(LocalDateTime.now());
            
            // Save to database for future searches
            return playerRepository.save(player);
        }
        
        return null;
    }
    
    private PlayerDto convertToDto(Player player) {
        PlayerDto dto = new PlayerDto();
        dto.setId(player.getId());
        dto.setSummonerName(player.getSummonerName());
        dto.setPuuid(player.getPuuid());
        dto.setSummonerId(player.getSummonerId());
        dto.setRegion(player.getRegion());
        dto.setSummonerLevel(player.getSummonerLevel());
        dto.setProfileIconUrl(player.getProfileIconUrl());
        dto.setLastUpdated(player.getLastUpdated());
        
        // Get recent champions (top 10 by games played)
        List<PlayerChampion> recentChampions = playerChampionRepository
            .findByPlayerOrderByGamesPlayedDesc(player)
            .stream()
            .limit(10)
            .collect(Collectors.toList());
        dto.setRecentChampions(recentChampions.stream()
            .map(this::convertToPlayerChampionDto)
            .collect(Collectors.toList()));
        
        // Get recent matches (last 20)
        List<PlayerMatch> recentMatches = playerMatchRepository
            .findByPlayerOrderByGameStartTimeDesc(player)
            .stream()
            .limit(20)
            .collect(Collectors.toList());
        dto.setRecentMatches(recentMatches.stream()
            .map(this::convertToPlayerMatchDto)
            .collect(Collectors.toList()));
        
        return dto;
    }

    private ChampionDto convertToChampionDto(Champion champion) {
        ChampionDto dto = new ChampionDto();
        dto.setId(champion.getId());
        dto.setChampionId(champion.getChampionId());
        dto.setName(champion.getName());
        dto.setTitle(champion.getTitle());
        dto.setImageUrl(champion.getImageUrl());
        dto.setSplashUrl(champion.getSplashUrl());
        dto.setTags(champion.getTags());
        dto.setLane(champion.getLane());
        dto.setRole(champion.getRole());
        return dto;
    }
    
    private PlayerChampionDto convertToPlayerChampionDto(PlayerChampion pc) {
        PlayerChampionDto dto = new PlayerChampionDto();
        dto.setId(pc.getId());
        dto.setChampion(convertToChampionDto(pc.getChampion())); // Changed this
        dto.setGamesPlayed(pc.getGamesPlayed());
        dto.setWins(pc.getWins());
        dto.setLosses(pc.getLosses());
        dto.setWinRate(pc.getWinRate());
        dto.setAverageKills(pc.getAverageKills());
        dto.setAverageDeaths(pc.getAverageDeaths());
        dto.setAverageAssists(pc.getAverageAssists());
        dto.setAverageCs(pc.getAverageCs());
        dto.setLastPlayed(pc.getLastPlayed());
        dto.setMasteryLevel(pc.getMasteryLevel());
        dto.setMasteryPoints(pc.getMasteryPoints());
        return dto;
    }
    
    private PlayerMatchDto convertToPlayerMatchDto(PlayerMatch pm) {
        PlayerMatchDto dto = new PlayerMatchDto();
        dto.setId(pm.getId());
        dto.setMatchId(pm.getMatchId());
        dto.setChampion(convertToChampionDto(pm.getChampion())); // Changed this
        dto.setGameMode(pm.getGameMode());
        dto.setGameType(pm.getGameType());
        dto.setGameStartTime(pm.getGameStartTime());
        dto.setGameDuration(pm.getGameDuration());
        dto.setKills(pm.getKills());
        dto.setDeaths(pm.getDeaths());
        dto.setAssists(pm.getAssists());
        dto.setCs(pm.getCs());
        dto.setWon(pm.getWon());
        dto.setLane(pm.getLane());
        dto.setRole(pm.getRole());
        dto.setTeamId(pm.getTeamId());
        return dto;
    }
    
    private PlayerSearchDto convertToPlayerSearchDto(Player player) {
        PlayerSearchDto dto = new PlayerSearchDto();
        dto.setSummonerName(player.getSummonerName());
        dto.setSummonerLevel(player.getSummonerLevel());
        dto.setProfileIconUrl(player.getProfileIconUrl());
        dto.setRegion(player.getRegion());
        
        // Parse summoner name to extract gameName and tagLine
        String[] parts = player.getSummonerName().split("#");
        dto.setGameName(parts[0]);
        dto.setTagLine(parts.length > 1 ? parts[1] : "");
        
        return dto;
    }
}
