package com.penta.dto;

import lombok.Data;

@Data
public class RiotSummonerDto {
    private String id;
    private String accountId;
    private String puuid;
    private String name;
    private int profileIconId;
    private long revisionDate;
    private int summonerLevel;
}
