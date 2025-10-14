package com.penta.dto;

import lombok.Data;

@Data
public class RiotChampionMasteryDto {
    private int championId;
    private int championLevel;
    private int championPoints;
    private long lastPlayTime;
    private int championPointsSinceLastLevel;
    private int championPointsUntilNextLevel;
    private boolean chestGranted;
    private int tokensEarned;
}
