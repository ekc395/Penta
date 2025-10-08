package com.penta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    private Long id;
    private String summonerName;
    private String puuid;
    private String summonerId;
    private String region;
    private Integer summonerLevel;
    private String profileIconUrl;
    private LocalDateTime lastUpdated;
    private List<PlayerChampionDto> recentChampions;
    private List<PlayerMatchDto> recentMatches;
}
