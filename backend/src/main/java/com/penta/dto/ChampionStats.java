package com.penta.dto;

public class ChampionStats {
    public final double winRate;
    public final int tier;
    
    public ChampionStats(double winRate, int tier) {
        this.winRate = winRate;
        this.tier = tier;
    }
}
