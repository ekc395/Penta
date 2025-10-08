package com.penta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionRecommendationDto {
    private ChampionDto champion;
    private Double recommendationScore;
    private String reason;
    private List<String> strengths;
    private List<String> weaknesses;
    private SynergyDto teamSynergy;
    private MatchupDto opponentMatchup;
    private PlayerComfortDto playerComfort;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SynergyDto {
    private List<ChampionDto> synergizesWith;
    private List<ChampionDto> conflictsWith;
    private Double synergyScore;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MatchupDto {
    private List<ChampionDto> strongAgainst;
    private List<ChampionDto> weakAgainst;
    private Double matchupScore;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerComfortDto {
    private Integer gamesPlayed;
    private Double winRate;
    private Integer masteryLevel;
    private Double comfortScore;
}
