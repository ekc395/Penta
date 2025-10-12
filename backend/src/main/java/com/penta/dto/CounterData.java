package com.penta.dto;

public class CounterData {
    private final String championName;
    private final double winRate;
    private final int gamesPlayed;

    public CounterData(String championName, double winRate, int gamesPlayed) {
        this.championName = championName;
        this.winRate = winRate;
        this.gamesPlayed = gamesPlayed;
    }
    
    public String getChampionName() { return championName; }
    public double getWinRate() { return winRate; }
    public int getGamesPlayed() { return gamesPlayed; }
    
    @Override
    public String toString() {
        return String.format("%s - WR: %.2f%% (%d games)", 
            championName, winRate, gamesPlayed);
    }
}
