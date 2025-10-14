package com.penta.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RiotChampionsDto {
    private Map<String, RiotChampionDto> data;
}
