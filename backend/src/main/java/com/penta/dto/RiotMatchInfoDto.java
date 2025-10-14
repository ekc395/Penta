package com.penta.dto;

import lombok.Data;
import java.util.List;

@Data
public class RiotMatchInfoDto {
    private String gameMode;
    private String gameType;
    private long gameStartTimestamp;
    private long gameDuration;
    private String platformId;
    private int seasonId;
    private int queueId;
    private int mapId;
    private String gameVersion;
    private List<RiotParticipantDto> participants;
}
