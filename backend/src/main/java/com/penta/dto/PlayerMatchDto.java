package com.penta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMatchDto {
    private Long id;
    private ChampionDto champion;
    private String matchId;
    private String gameMode;
    private String gameType;
    private LocalDateTime gameStartTime;
    private Long gameDuration;
    private Boolean won;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer cs;
    private String lane;
    private String role;
    private Integer teamId;
}
