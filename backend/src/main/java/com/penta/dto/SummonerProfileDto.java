package com.penta.dto;

public class SummonerProfileDto {
    private String summonerName;
    private String region;
    private String summonerId;
    private String puuid;
    private int summonerLevel;
    private String profileIconUrl;

    public String getSummonerName() { return summonerName; }
    public void setSummonerName(String summonerName) { this.summonerName = summonerName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getSummonerId() { return summonerId; }
    public void setSummonerId(String summonerId) { this.summonerId = summonerId; }
    public String getPuuid() { return puuid; }
    public void setPuuid(String puuid) { this.puuid = puuid; }
    public int getSummonerLevel() { return summonerLevel; }
    public void setSummonerLevel(int summonerLevel) { this.summonerLevel = summonerLevel; }
    public String getProfileIconUrl() { return profileIconUrl; }
    public void setProfileIconUrl(String profileIconUrl) { this.profileIconUrl = profileIconUrl; }
}