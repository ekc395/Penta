package com.penta.controller;

import com.penta.model.Match;
import com.penta.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matches")
@CrossOrigin(origins = "http://localhost:5173")
public class MatchController {
    
    @Autowired
    private MatchRepository matchRepository;
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllMatches() {
        List<Map<String, Object>> matches = matchRepository.findAll().stream()
                .map(m -> {
                    Map<String, Object> matchMap = new HashMap<>();
                    matchMap.put("matchId", m.getMatchId());
                    matchMap.put("gameMode", m.getGameMode());
                    matchMap.put("gameType", m.getGameType());
                    matchMap.put("gameDuration", m.getGameDuration());
                    matchMap.put("gameStartTime", m.getGameStartTime().toString());
                    matchMap.put("queueId", m.getQueueId());
                    matchMap.put("participantCount", m.getParticipants() != null ? m.getParticipants().size() : 0);
                    return matchMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(matches);
    }
    
    @GetMapping("/{matchId}")
    public ResponseEntity<Match> getMatch(@PathVariable String matchId) {
        return matchRepository.findByMatchId(matchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}