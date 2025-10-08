package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "champion_matchups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionMatchup {
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
    private String role;
    
    @Column(nullable = false)
    private Integer totalGames;
    
    @Column(nullable = false)
    private Integer champion1Wins;
    
    @Column(nullable = false)
    private Integer champion2Wins;
    
    @Column(nullable = false)
    private Double champion1WinRate;
    
    @Column(nullable = false)
    private Double champion2WinRate;
    
    @Column(nullable = false)
    private Double matchupScore; // -1 to 1, where 1 means champion1 strongly beats champion2
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    // Ensure unique combination of champions, patch, rank, and role
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"champion1_id", "champion2_id", "patch", "rank", "role"})
    })
    public static class ChampionMatchupTable {
    }
}
