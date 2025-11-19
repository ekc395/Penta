package com.penta.dto;

import lombok.Data;

@Data
public class RiotParticipantDto {
    private int participantId;
    private String puuid;
    private String summonerId;
    private String summonerName;
    private String riotIdGameName;  
    private String riotIdTagline;  
    private int championId;
    private int teamId;
    private String individualPosition;
    private String teamPosition;
    private boolean win;
    private int kills;
    private int deaths;
    private int assists;
    private int totalMinionsKilled;
    private int neutralMinionsKilled;
    private long goldEarned;
    private long totalDamageDealtToChampions;
    private long totalDamageTaken;
    private long visionScore;
    private int wardsPlaced;
    private int wardsKilled;
    private boolean firstBloodKill;
    private boolean firstTowerKill;
    private int champLevel;
    private int item0;
    private int item1;
    private int item2;
    private int item3;
    private int item4;
    private int item5;
    private int item6;
    private int summoner1Id;
    private int summoner2Id;
    private int primaryStyle;
    private int subStyle;
}