package com.penta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchParticipantDto {
    private String summonerName;
    private ChampionDto champion;
    private Integer teamId;
    private Boolean won;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer cs;
    private String lane;
    private Long damageDealt;
    private Long damageTaken;
    private Long goldEarned;
    private Long visionScore;
    private Integer wardsPlaced;
    private Integer wardsKilled;
    private Integer champLevel;
    private Integer item0;
    private Integer item1;
    private Integer item2;
    private Integer item3;
    private Integer item4;
    private Integer item5;
    private Integer item6;
}
