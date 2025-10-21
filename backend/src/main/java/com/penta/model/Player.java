package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String summonerName;
    
    @Column
    private String puuid;
    
    @Column
    private String summonerId;
    
    @Column
    private String region;
    
    @Column
    private Integer summonerLevel;
    
    @Column
    private String profileIconUrl;
    
    @Column
    private LocalDateTime lastUpdated;
    
    @Column
    private LocalDateTime lastAccessed;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlayerChampion> recentChampions;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlayerMatch> recentMatches;
}
