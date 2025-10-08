package com.penta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionDto {
    private Long id;
    private Integer championId;
    private String name;
    private String title;
    private String role;
    private String lane;
    private String imageUrl;
    private String splashUrl;
    private Double winRate;
    private Double pickRate;
    private Double banRate;
    private Integer tier;
    private String tags;
}
