package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "champion_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false)
    private Champion champion;
    
    @Column(nullable = false)
    private String patch;
    
    @Column(nullable = false)
    private String rank;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private Integer totalGames;
    
    @Column(nullable = false)
    private Integer wins;
    
    @Column(nullable = false)
    private Integer losses;
    
    @Column(nullable = false)
    private Double winRate;
    
    @Column(nullable = false)
    private Double pickRate;
    
    @Column(nullable = false)
    private Double banRate;
    
    @Column(nullable = false)
    private Double averageKills;
    
    @Column(nullable = false)
    private Double averageDeaths;
    
    @Column(nullable = false)
    private Double averageAssists;
    
    @Column(nullable = false)
    private Double averageCs;
    
    @Column(nullable = false)
    private Double averageGold;
    
    @Column(nullable = false)
    private Double averageDamage;
    
    @Column(nullable = false)
    private Double averageVisionScore;
    
    @Column(nullable = false)
    private Integer tier;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
}
