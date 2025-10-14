package com.penta.dto;

import lombok.Data;

@Data
public class RiotChampionDto {
    private String id;
    private String key;
    private String name;
    private String title;
    private RiotChampionImageDto image;
    private String[] tags;
}
