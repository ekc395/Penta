package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @Column(nullable = false)
    private Integer teamId;
    
    @Column(nullable = false)
    private Boolean won;
    
    @Column(nullable = false)
    private Integer baronKills;
    
    @Column(nullable = false)
    private Integer dragonKills;
    
    @Column(nullable = false)
    private Integer riftHeraldKills;
    
    @Column(nullable = false)
    private Integer towerKills;
    
    @Column(nullable = false)
    private Integer inhibitorKills;
    
    @Column(nullable = false)
    private Integer totalKills;
    
    @Column(nullable = false)
    private Integer totalDeaths;
    
    @Column(nullable = false)
    private Integer totalAssists;
    
    @Column(nullable = false)
    private Long totalGold;
    
    @Column(nullable = false)
    private Long totalDamage;
    
    @Column(nullable = false)
    private Long totalMinionsKilled;
    
    @Column(nullable = false)
    private Long totalNeutralMinionsKilled;
    
    @Column(nullable = false)
    private Long totalVisionScore;
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchParticipant> participants;
}
