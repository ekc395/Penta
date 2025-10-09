package com.penta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id_fk")
    private Team team;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false)
    private Champion champion;
    
    @Column(nullable = false)
    private Integer teamId;
    
    // ... rest of your fields stay the same
    @Column(nullable = false)
    private Integer participantId;
    
    @Column(nullable = false)
    private String summonerId;
    
    @Column(nullable = false)
    private String puuid;
    
    @Column(nullable = false)
    private String summonerName;
    
    @Column(nullable = false)
    private String individualPosition;
    
    @Column(nullable = false)
    private String teamPosition;
    
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
    
    @Column(nullable = false)
    private Long goldEarned;
    
    @Column(nullable = false)
    private Long damageDealt;
    
    @Column(nullable = false)
    private Long damageTaken;
    
    @Column(nullable = false)
    private Long visionScore;
    
    @Column(nullable = false)
    private Integer wardsPlaced;
    
    @Column(nullable = false)
    private Integer wardsKilled;
    
    @Column(nullable = false)
    private Integer firstBloodKill;
    
    @Column(nullable = false)
    private Integer firstTowerKill;
    
    @Column(nullable = false)
    private Integer totalMinionsKilled;
    
    @Column(nullable = false)
    private Integer neutralMinionsKilled;
    
    @Column(nullable = false)
    private Integer champLevel;
    
    @Column(nullable = false)
    private Integer item0;
    
    @Column(nullable = false)
    private Integer item1;
    
    @Column(nullable = false)
    private Integer item2;
    
    @Column(nullable = false)
    private Integer item3;
    
    @Column(nullable = false)
    private Integer item4;
    
    @Column(nullable = false)
    private Integer item5;
    
    @Column(nullable = false)
    private Integer item6;
    
    @Column(nullable = false)
    private Integer summoner1Id;
    
    @Column(nullable = false)
    private Integer summoner2Id;
    
    @Column(nullable = false)
    private Integer primaryPerk;
    
    @Column(nullable = false)
    private Integer subPerk;
}