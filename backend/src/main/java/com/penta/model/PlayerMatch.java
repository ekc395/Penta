package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false)
    private Champion champion;
    
    // Add this - reference to the full Match with all participants
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id_fk", referencedColumnName = "matchId")
    private Match match;
    
    @Column(nullable = false)
    private String matchId;
    
    @Column(nullable = false)
    private String gameMode;
    
    @Column(nullable = false)
    private String gameType;
    
    @Column(nullable = false)
    private LocalDateTime gameStartTime;
    
    @Column(nullable = false)
    private Long gameDuration;
    
    @Column(nullable = false)
    private Boolean won;
    
    @Column(nullable = false)
    private Integer kills;
    
    @Column(nullable = false)
    private Integer deaths;
    
    @Column(nullable = false)
    private Integer assists;
    
    @Column(nullable = false)
    private Integer cs;
    
    @Column
    private String lane;
    
    @Column
    private String role;
    
    @Column
    private Integer teamId;
    
    // Add these fields:
    @Column
    private Long damageDealt;
    
    @Column
    private Long damageTaken;
    
    @Column
    private Long goldEarned;
    
    @Column
    private Long visionScore;
    
    @Column
    private Integer wardsPlaced;
    
    @Column
    private Integer wardsKilled;
}