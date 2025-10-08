package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
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
    private String platformId;
    
    @Column(nullable = false)
    private Integer seasonId;
    
    @Column(nullable = false)
    private Integer queueId;
    
    @Column(nullable = false)
    private String mapId;
    
    @Column(nullable = false)
    private String gameVersion;
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchParticipant> participants;
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Team> teams;
}
