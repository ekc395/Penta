package com.penta.controller;

import com.penta.model.Champion;
import com.penta.dto.PlayerDto;
import com.penta.dto.ChampionDto;
import com.penta.dto.PlayerChampionDto;
import com.penta.dto.PlayerMatchDto;
import com.penta.model.Player;
import com.penta.model.PlayerChampion;
import com.penta.model.PlayerMatch;
import com.penta.repository.PlayerRepository;
import com.penta.repository.PlayerChampionRepository;
import com.penta.repository.PlayerMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
