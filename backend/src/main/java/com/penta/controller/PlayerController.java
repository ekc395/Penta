package com.penta.controller;

import com.penta.dto.PlayerDto;
import com.penta.model.Player;
import com.penta.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/players")
@CrossOrigin(origins = "http://localhost:5173")
public class PlayerController {
    
    @Autowired
    private PlayerRepository playerRepository;
    
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
        return dto;
    }
}
