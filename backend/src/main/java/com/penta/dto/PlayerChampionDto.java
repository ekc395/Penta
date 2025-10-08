package com.penta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerChampionDto {
    private Long id;
    private ChampionDto champion;
    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private Double winRate;
    private Double averageKills;
    private Double averageDeaths;
    private Double averageAssists;
    private Double averageCs;
    private LocalDateTime lastPlayed;
    private Integer masteryLevel;
    private Integer masteryPoints;
}
