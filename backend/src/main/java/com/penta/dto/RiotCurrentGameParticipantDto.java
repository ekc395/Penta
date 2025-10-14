package com.penta.dto;

import lombok.Data;

@Data
public class RiotCurrentGameParticipantDto {
    private int championId;
    private int teamId;
    private String summonerName;
    private String summonerId;
    private int profileIconId;
    private boolean bot;
    private int spell1Id;
    private int spell2Id;
}
