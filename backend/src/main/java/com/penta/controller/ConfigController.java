package com.penta.controller;

import com.penta.config.RiotApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
@CrossOrigin(origins = "http://localhost:5173")
public class ConfigController {
    
    @Autowired
    private RiotApiConfig riotApiConfig;
    
    @GetMapping("/ddragon-version")
    public ResponseEntity<Map<String, String>> getDdragonVersion() {
        Map<String, String> response = new HashMap<>();
        response.put("version", riotApiConfig.getDdragonVersion());
        return ResponseEntity.ok(response);
    }
}
