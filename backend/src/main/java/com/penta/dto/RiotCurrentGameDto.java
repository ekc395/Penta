package com.penta.dto;

import lombok.Data;
import java.util.List;

@Data
public class RiotCurrentGameDto {
    private long gameId;
    private String gameType;
    private long gameStartTime;
    private long mapId;
    private long gameLength;
    private String platformId;
    private String gameMode;
    private List<RiotCurrentGameParticipantDto> participants;
}
