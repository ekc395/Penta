package com.penta.dto;

public class PlayerSearchDto {
    private String summonerName;
    private String gameName;
    private String tagLine;
    private int summonerLevel;
    private String profileIconUrl;
    private String region;
    
    // Constructors
    public PlayerSearchDto() {}
    
    public PlayerSearchDto(String summonerName, String gameName, String tagLine, 
                          int summonerLevel, String profileIconUrl, String region) {
        this.summonerName = summonerName;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.summonerLevel = summonerLevel;
        this.profileIconUrl = profileIconUrl;
        this.region = region;
    }
    
    // Getters and Setters
    public String getSummonerName() {
        return summonerName;
    }
    
    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }
    
    public String getGameName() {
        return gameName;
    }
    
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    
    public String getTagLine() {
        return tagLine;
    }
    
    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }
    
    public int getSummonerLevel() {
        return summonerLevel;
    }
    
    public void setSummonerLevel(int summonerLevel) {
        this.summonerLevel = summonerLevel;
    }
    
    public String getProfileIconUrl() {
        return profileIconUrl;
    }
    
    public void setProfileIconUrl(String profileIconUrl) {
        this.profileIconUrl = profileIconUrl;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
}

