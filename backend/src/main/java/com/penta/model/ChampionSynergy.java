package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "champion_synergies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionSynergy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion1_id", nullable = false)
    private Champion champion1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion2_id", nullable = false)
    private Champion champion2;
    
    @Column(nullable = false)
    private String patch;
    
    @Column(nullable = false)
    private String rank;
    
    @Column(nullable = false)
    private Integer totalGames;
    
    @Column(nullable = false)
    private Integer wins;
    
    @Column(nullable = false)
    private Integer losses;
    
    @Column(nullable = false)
    private Double winRate;
    
    @Column(nullable = false)
    private Double synergyScore; // 0 to 1, where 1 means perfect synergy
    
    @Column(nullable = false)
    private String synergyType; // "LANE", "TEAM", "COMBO"
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    // Ensure unique combination of champions, patch, and rank
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"champion1_id", "champion2_id", "patch", "rank"})
    })
    public static class ChampionSynergyTable {
    }
}
