package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_champions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerChampion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false)
    private Champion champion;
    
    @Column(nullable = false)
    private Integer gamesPlayed;
    
    @Column(nullable = false)
    private Integer wins;
    
    @Column(nullable = false)
    private Integer losses;
    
    @Column
    private Double winRate;
    
    @Column
    private Double averageKills;
    
    @Column
    private Double averageDeaths;
    
    @Column
    private Double averageAssists;
    
    @Column
    private Double averageCs;
    
    @Column
    private LocalDateTime lastPlayed;
    
    @Column
    private Integer masteryLevel;
    
    @Column
    private Integer masteryPoints;
}
