package com.penta.service;

import com.penta.config.RiotApiConfig;
import com.penta.model.Champion;
import com.penta.model.Match;
import com.penta.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class RiotApiService {
    
    @Autowired
    private RiotApiConfig riotApiConfig;
    
    @Autowired
    private WebClient riotWebClient;
    
    /**
     * Get player information by summoner name
     */
    public Optional<Player> getPlayerBySummonerName(String summonerName, String region) {
        try {
            String url = String.format("/lol/summoner/v4/summoners/by-name/%s", summonerName);
            
            RiotSummonerDto summoner = riotWebClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RiotSummonerDto.class)
                    .block();
            
            if (summoner != null) {
                Player player = new Player();
                player.setSummonerName(summonerName);
                player.setPuuid(summoner.getPuuid());
                player.setSummonerId(summoner.getId());
                player.setRegion(region);
                player.setSummonerLevel(summoner.getSummonerLevel());
                player.setProfileIconUrl(String.format("https://ddragon.leagueoflegends.com/cdn/13.24.1/img/profileicon/%d.png", 
                        summoner.getProfileIconId()));
                player.setLastUpdated(LocalDateTime.now());
                
                return Optional.of(player);
            }
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("Error fetching player data: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get match history for a player
     */
    public List<String> getMatchHistory(String puuid, String region, int count) {
        try {
            String url = String.format("/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%d", puuid, count);
            
            List<String> matchIds = riotWebClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .collectList()
                    .block();
            
            return matchIds != null ? matchIds : List.of();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error fetching match history: " + e.getMessage());
        }
    }
    
    /**
     * Get detailed match information
     */
    public Optional<Match> getMatchDetails(String matchId, String region) {
        try {
            String url = String.format("/lol/match/v5/matches/%s", matchId);
            
            RiotMatchDto matchDto = riotWebClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RiotMatchDto.class)
                    .block();
            
            if (matchDto != null) {
                return Optional.of(convertToMatch(matchDto));
            }
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("Error fetching match details: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get champion mastery for a player
     */
    public List<RiotChampionMasteryDto> getChampionMastery(String summonerId, String region) {
        try {
            String url = String.format("/lol/champion-mastery/v4/champion-masteries/by-summoner/%s", summonerId);
            
            List<RiotChampionMasteryDto> mastery = riotWebClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(RiotChampionMasteryDto.class)
                    .collectList()
                    .block();
            
            return mastery != null ? mastery : List.of();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error fetching champion mastery: " + e.getMessage());
        }
    }
    
    /**
     * Get current game information
     */
    public Optional<RiotCurrentGameDto> getCurrentGame(String summonerId, String region) {
        try {
            String url = String.format("/lol/spectator/v4/active-games/by-summoner/%s", summonerId);
            
            RiotCurrentGameDto currentGame = riotWebClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RiotCurrentGameDto.class)
                    .block();
            
            return Optional.ofNullable(currentGame);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("Error fetching current game: " + e.getMessage());
        }
    }
    
    /**
     * Get all champions data from Data Dragon
     */
    public List<Champion> getAllChampions() {
        try {
            // Use Data Dragon instead of the API
            String url = "https://ddragon.leagueoflegends.com/cdn/14.1.1/data/en_US/champion.json";
            
            WebClient ddragonClient = WebClient.create();
            RiotChampionsDto champions = ddragonClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RiotChampionsDto.class)
                    .block();
            
            if (champions != null && champions.getData() != null) {
                return champions.getData().values().stream()
                        .map(this::convertToChampion)
                        .toList();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching champions: " + e.getMessage());
        }
        
        return List.of();
    }
    
    private Match convertToMatch(RiotMatchDto matchDto) {
        Match match = new Match();
        match.setMatchId(matchDto.getMetadata().getMatchId());
        match.setGameMode(matchDto.getInfo().getGameMode());
        match.setGameType(matchDto.getInfo().getGameType());
        match.setGameStartTime(LocalDateTime.ofEpochSecond(matchDto.getInfo().getGameStartTimestamp() / 1000, 0, ZoneOffset.UTC));
        match.setGameDuration(matchDto.getInfo().getGameDuration());
        match.setPlatformId(matchDto.getInfo().getPlatformId());
        match.setSeasonId(matchDto.getInfo().getSeasonId());
        match.setQueueId(matchDto.getInfo().getQueueId());
        match.setMapId(String.valueOf(matchDto.getInfo().getMapId()));
        match.setGameVersion(matchDto.getInfo().getGameVersion());
        
        return match;
    }
    
    private Champion convertToChampion(RiotChampionDto championDto) {
        Champion champion = new Champion();
        champion.setChampionId(championDto.getKey() != null ? Integer.parseInt(championDto.getKey()) : 0);
        champion.setName(championDto.getName());
        champion.setTitle(championDto.getTitle());
        champion.setImageUrl(String.format("https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/%s.png", 
                championDto.getImage().getFull()));
        champion.setSplashUrl(String.format("https://ddragon.leagueoflegends.com/cdn/img/champion/splash/%s_0.jpg", 
                championDto.getId()));
        champion.setTags(String.join(",", championDto.getTags()));
        

        // Set default values for required fields
        champion.setLane("ALL");
        champion.setRole("ALL");
        
        return champion;
    }
    
    // DTOs for Riot API responses
    public static class RiotSummonerDto {
        private String id;
        private String accountId;
        private String puuid;
        private String name;
        private int profileIconId;
        private long revisionDate;
        private int summonerLevel;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        public String getPuuid() { return puuid; }
        public void setPuuid(String puuid) { this.puuid = puuid; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getProfileIconId() { return profileIconId; }
        public void setProfileIconId(int profileIconId) { this.profileIconId = profileIconId; }
        public long getRevisionDate() { return revisionDate; }
        public void setRevisionDate(long revisionDate) { this.revisionDate = revisionDate; }
        public int getSummonerLevel() { return summonerLevel; }
        public void setSummonerLevel(int summonerLevel) { this.summonerLevel = summonerLevel; }
    }
    
    public static class RiotMatchDto {
        private RiotMatchMetadataDto metadata;
        private RiotMatchInfoDto info;
        
        public RiotMatchMetadataDto getMetadata() { return metadata; }
        public void setMetadata(RiotMatchMetadataDto metadata) { this.metadata = metadata; }
        public RiotMatchInfoDto getInfo() { return info; }
        public void setInfo(RiotMatchInfoDto info) { this.info = info; }
    }
    
    public static class RiotMatchMetadataDto {
        private String matchId;
        
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
    }
    
    public static class RiotMatchInfoDto {
        private String gameMode;
        private String gameType;
        private long gameStartTimestamp;
        private long gameDuration;
        private String platformId;
        private int seasonId;
        private int queueId;
        private int mapId;
        private String gameVersion;
        
        // Getters and setters
        public String getGameMode() { return gameMode; }
        public void setGameMode(String gameMode) { this.gameMode = gameMode; }
        public String getGameType() { return gameType; }
        public void setGameType(String gameType) { this.gameType = gameType; }
        public long getGameStartTimestamp() { return gameStartTimestamp; }
        public void setGameStartTimestamp(long gameStartTimestamp) { this.gameStartTimestamp = gameStartTimestamp; }
        public long getGameDuration() { return gameDuration; }
        public void setGameDuration(long gameDuration) { this.gameDuration = gameDuration; }
        public String getPlatformId() { return platformId; }
        public void setPlatformId(String platformId) { this.platformId = platformId; }
        public int getSeasonId() { return seasonId; }
        public void setSeasonId(int seasonId) { this.seasonId = seasonId; }
        public int getQueueId() { return queueId; }
        public void setQueueId(int queueId) { this.queueId = queueId; }
        public int getMapId() { return mapId; }
        public void setMapId(int mapId) { this.mapId = mapId; }
        public String getGameVersion() { return gameVersion; }
        public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }
    }
    
    public static class RiotChampionMasteryDto {
        private int championId;
        private int championLevel;
        private int championPoints;
        private long lastPlayTime;
        private int championPointsSinceLastLevel;
        private int championPointsUntilNextLevel;
        private boolean chestGranted;
        private int tokensEarned;
        
        // Getters and setters
        public int getChampionId() { return championId; }
        public void setChampionId(int championId) { this.championId = championId; }
        public int getChampionLevel() { return championLevel; }
        public void setChampionLevel(int championLevel) { this.championLevel = championLevel; }
        public int getChampionPoints() { return championPoints; }
        public void setChampionPoints(int championPoints) { this.championPoints = championPoints; }
        public long getLastPlayTime() { return lastPlayTime; }
        public void setLastPlayTime(long lastPlayTime) { this.lastPlayTime = lastPlayTime; }
        public int getChampionPointsSinceLastLevel() { return championPointsSinceLastLevel; }
        public void setChampionPointsSinceLastLevel(int championPointsSinceLastLevel) { this.championPointsSinceLastLevel = championPointsSinceLastLevel; }
        public int getChampionPointsUntilNextLevel() { return championPointsUntilNextLevel; }
        public void setChampionPointsUntilNextLevel(int championPointsUntilNextLevel) { this.championPointsUntilNextLevel = championPointsUntilNextLevel; }
        public boolean isChestGranted() { return chestGranted; }
        public void setChestGranted(boolean chestGranted) { this.chestGranted = chestGranted; }
        public int getTokensEarned() { return tokensEarned; }
        public void setTokensEarned(int tokensEarned) { this.tokensEarned = tokensEarned; }
    }
    
    public static class RiotCurrentGameDto {
        private long gameId;
        private String gameType;
        private long gameStartTime;
        private long mapId;
        private long gameLength;
        private String platformId;
        private String gameMode;
        private List<RiotCurrentGameParticipantDto> participants;
        
        // Getters and setters
        public long getGameId() { return gameId; }
        public void setGameId(long gameId) { this.gameId = gameId; }
        public String getGameType() { return gameType; }
        public void setGameType(String gameType) { this.gameType = gameType; }
        public long getGameStartTime() { return gameStartTime; }
        public void setGameStartTime(long gameStartTime) { this.gameStartTime = gameStartTime; }
        public long getMapId() { return mapId; }
        public void setMapId(long mapId) { this.mapId = mapId; }
        public long getGameLength() { return gameLength; }
        public void setGameLength(long gameLength) { this.gameLength = gameLength; }
        public String getPlatformId() { return platformId; }
        public void setPlatformId(String platformId) { this.platformId = platformId; }
        public String getGameMode() { return gameMode; }
        public void setGameMode(String gameMode) { this.gameMode = gameMode; }
        public List<RiotCurrentGameParticipantDto> getParticipants() { return participants; }
        public void setParticipants(List<RiotCurrentGameParticipantDto> participants) { this.participants = participants; }
    }
    
    public static class RiotCurrentGameParticipantDto {
        private int championId;
        private int teamId;
        private String summonerName;
        private String summonerId;
        private int profileIconId;
        private boolean bot;
        private int spell1Id;
        private int spell2Id;
        
        // Getters and setters
        public int getChampionId() { return championId; }
        public void setChampionId(int championId) { this.championId = championId; }
        public int getTeamId() { return teamId; }
        public void setTeamId(int teamId) { this.teamId = teamId; }
        public String getSummonerName() { return summonerName; }
        public void setSummonerName(String summonerName) { this.summonerName = summonerName; }
        public String getSummonerId() { return summonerId; }
        public void setSummonerId(String summonerId) { this.summonerId = summonerId; }
        public int getProfileIconId() { return profileIconId; }
        public void setProfileIconId(int profileIconId) { this.profileIconId = profileIconId; }
        public boolean isBot() { return bot; }
        public void setBot(boolean bot) { this.bot = bot; }
        public int getSpell1Id() { return spell1Id; }
        public void setSpell1Id(int spell1Id) { this.spell1Id = spell1Id; }
        public int getSpell2Id() { return spell2Id; }
        public void setSpell2Id(int spell2Id) { this.spell2Id = spell2Id; }
    }
    
    public static class RiotChampionsDto {
        private java.util.Map<String, RiotChampionDto> data;
        
        public java.util.Map<String, RiotChampionDto> getData() { return data; }
        public void setData(java.util.Map<String, RiotChampionDto> data) { this.data = data; }
    }
    
    public static class RiotChampionDto {
        private String id;
        private String key;
        private String name;
        private String title;
        private RiotChampionImageDto image;
        private String[] tags;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public RiotChampionImageDto getImage() { return image; }
        public void setImage(RiotChampionImageDto image) { this.image = image; }
        public String[] getTags() { return tags; }
        public void setTags(String[] tags) { this.tags = tags; }
    }
    
    public static class RiotChampionImageDto {
        private String full;
        
        public String getFull() { return full; }
        public void setFull(String full) { this.full = full; }
    }
}