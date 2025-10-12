package com.penta.service;

import com.penta.config.RiotApiConfig;
import com.penta.model.Champion;
import com.penta.model.Match;
import com.penta.model.MatchParticipant;
import com.penta.model.Player;
import com.penta.repository.ChampionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RiotApiService {
    
    @Autowired
    private RiotApiConfig riotApiConfig;
    
    @Autowired
    private WebClient riotWebClient;
    
    @Autowired
    private ChampionRepository championRepository;
    
    /**
     * Get player information by summoner name
     */
    public Optional<Player> getPlayerBySummonerName(String summonerName, String region) {
        try {
            // Split into gameName and tagLine
            String[] parts = summonerName.split("#");
            String gameName = parts[0];
            String tagLine = parts.length > 1 ? parts[1] : region.toUpperCase();
            
            // First, get PUUID from Account API
            String accountUrl = String.format("https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s", gameName, tagLine);
            
            WebClient regionalClient = WebClient.builder()
                    .defaultHeader("X-Riot-Token", riotApiConfig.getRiotApiKey())
                    .build();
            
            RiotAccountDto account = regionalClient
                    .get()
                    .uri(accountUrl)
                    .retrieve()
                    .bodyToMono(RiotAccountDto.class)
                    .block();
            
            if (account == null) {
                return Optional.empty();
            }
            
            // Then get summoner data using PUUID
            String summonerUrl = String.format("https://%s.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s", region, account.getPuuid());
            
            RiotSummonerDto summoner = regionalClient
                    .get()
                    .uri(summonerUrl)
                    .retrieve()
                    .bodyToMono(RiotSummonerDto.class)
                    .block();
            
            if (summoner != null) {
                Player player = new Player();
                player.setSummonerName(gameName + "#" + tagLine);
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
            
            // Use ParameterizedTypeReference to properly parse JSON array of strings
            List<String> matchIds = riotWebClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<List<String>>() {})
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
            String url = String.format("https://%s.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/%s", region, summonerId);
            
            WebClient regionalClient = WebClient.builder()
                    .defaultHeader("X-Riot-Token", riotApiConfig.getRiotApiKey())
                    .build();
            
            List<RiotChampionMasteryDto> mastery = regionalClient
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
            String url = String.format("https://%s.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/%s", region, summonerId);
            
            WebClient regionalClient = WebClient.builder()
                    .defaultHeader("X-Riot-Token", riotApiConfig.getRiotApiKey())
                    .build();
            
            RiotCurrentGameDto currentGame = regionalClient
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
        
        // Convert participants
        List<MatchParticipant> participants = new ArrayList<>();
        if (matchDto.getInfo().getParticipants() != null) {
            for (RiotParticipantDto participantDto : matchDto.getInfo().getParticipants()) {
                MatchParticipant participant = convertToMatchParticipant(participantDto, match);
                if (participant != null) {
                    participants.add(participant);
                }
            }
        }
        match.setParticipants(participants);
        
        return match;
    }
    
    private MatchParticipant convertToMatchParticipant(RiotParticipantDto dto, Match match) {
        // Find champion by ID
        Optional<Champion> championOpt = championRepository.findByChampionId(dto.getChampionId());
        if (championOpt.isEmpty()) {
            return null; // Skip if champion not found
        }
        
        MatchParticipant participant = new MatchParticipant();
        participant.setMatch(match);
        participant.setChampion(championOpt.get());
        participant.setParticipantId(dto.getParticipantId());
        participant.setSummonerId(dto.getSummonerId());
        participant.setPuuid(dto.getPuuid());
        participant.setSummonerName(dto.getSummonerName());
        participant.setTeamId(dto.getTeamId());
        participant.setIndividualPosition(dto.getIndividualPosition());
        participant.setTeamPosition(dto.getTeamPosition());
        participant.setWon(dto.isWin());
        participant.setKills(dto.getKills());
        participant.setDeaths(dto.getDeaths());
        participant.setAssists(dto.getAssists());
        participant.setCs(dto.getTotalMinionsKilled() + dto.getNeutralMinionsKilled());
        participant.setGoldEarned(dto.getGoldEarned());
        participant.setDamageDealt(dto.getTotalDamageDealtToChampions());
        participant.setDamageTaken(dto.getTotalDamageTaken());
        participant.setVisionScore(dto.getVisionScore());
        participant.setWardsPlaced(dto.getWardsPlaced());
        participant.setWardsKilled(dto.getWardsKilled());
        participant.setFirstBloodKill(dto.getFirstBloodKill());
        participant.setFirstTowerKill(dto.getFirstTowerKill());
        participant.setTotalMinionsKilled(dto.getTotalMinionsKilled());
        participant.setNeutralMinionsKilled(dto.getNeutralMinionsKilled());
        participant.setChampLevel(dto.getChampLevel());
        participant.setItem0(dto.getItem0());
        participant.setItem1(dto.getItem1());
        participant.setItem2(dto.getItem2());
        participant.setItem3(dto.getItem3());
        participant.setItem4(dto.getItem4());
        participant.setItem5(dto.getItem5());
        participant.setItem6(dto.getItem6());
        participant.setSummoner1Id(dto.getSummoner1Id());
        participant.setSummoner2Id(dto.getSummoner2Id());
        participant.setPrimaryPerk(dto.getPrimaryStyle());
        participant.setSubPerk(dto.getSubStyle());
        
        return participant;
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
        private List<RiotParticipantDto> participants; // ADD THIS
        
        // Add getter/setter
        public List<RiotParticipantDto> getParticipants() { return participants; }
        public void setParticipants(List<RiotParticipantDto> participants) { this.participants = participants; }
    
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

    public static class RiotAccountDto {
        private String puuid;
        private String gameName;
        private String tagLine;
        
        public String getPuuid() { return puuid; }
        public void setPuuid(String puuid) { this.puuid = puuid; }
        public String getGameName() { return gameName; }
        public void setGameName(String gameName) { this.gameName = gameName; }
        public String getTagLine() { return tagLine; }
        public void setTagLine(String tagLine) { this.tagLine = tagLine; }
    }

    public static class RiotParticipantDto {
        private int participantId;
        private String puuid;
        private String summonerId;
        private String summonerName;
        private int championId;
        private int teamId;
        private String individualPosition;
        private String teamPosition;
        private boolean win;
        private int kills;
        private int deaths;
        private int assists;
        private int totalMinionsKilled;
        private int neutralMinionsKilled;
        private long goldEarned;
        private long totalDamageDealtToChampions;
        private long totalDamageTaken;
        private long visionScore;
        private int wardsPlaced;
        private int wardsKilled;
        private boolean firstBloodKill;
        private boolean firstTowerKill;
        private int champLevel; 
        private int item0;
        private int item1;
        private int item2;
        private int item3;
        private int item4;
        private int item5;
        private int item6;
        private int summoner1Id;
        private int summoner2Id;
        private int primaryStyle;
        private int subStyle;
        
        // Getters and setters for all fields
        public int getParticipantId() { return participantId; }
        public void setParticipantId(int participantId) { this.participantId = participantId; }
        public String getPuuid() { return puuid; }
        public void setPuuid(String puuid) { this.puuid = puuid; }
        public String getSummonerId() { return summonerId; }
        public void setSummonerId(String summonerId) { this.summonerId = summonerId; }
        public String getSummonerName() { return summonerName; }
        public void setSummonerName(String summonerName) { this.summonerName = summonerName; }
        public int getChampionId() { return championId; }
        public void setChampionId(int championId) { this.championId = championId; }
        public int getTeamId() { return teamId; }
        public void setTeamId(int teamId) { this.teamId = teamId; }
        public String getIndividualPosition() { return individualPosition; }
        public void setIndividualPosition(String individualPosition) { this.individualPosition = individualPosition; }
        public String getTeamPosition() { return teamPosition; }
        public void setTeamPosition(String teamPosition) { this.teamPosition = teamPosition; }
        public boolean isWin() { return win; }
        public void setWin(boolean win) { this.win = win; }
        public int getKills() { return kills; }
        public void setKills(int kills) { this.kills = kills; }
        public int getDeaths() { return deaths; }
        public void setDeaths(int deaths) { this.deaths = deaths; }
        public int getAssists() { return assists; }
        public void setAssists(int assists) { this.assists = assists; }
        public int getTotalMinionsKilled() { return totalMinionsKilled; }
        public void setTotalMinionsKilled(int totalMinionsKilled) { this.totalMinionsKilled = totalMinionsKilled; }
        public int getNeutralMinionsKilled() { return neutralMinionsKilled; }
        public void setNeutralMinionsKilled(int neutralMinionsKilled) { this.neutralMinionsKilled = neutralMinionsKilled; }
        public long getGoldEarned() { return goldEarned; }
        public void setGoldEarned(long goldEarned) { this.goldEarned = goldEarned; }
        public long getTotalDamageDealtToChampions() { return totalDamageDealtToChampions; }
        public void setTotalDamageDealtToChampions(long totalDamageDealtToChampions) { this.totalDamageDealtToChampions = totalDamageDealtToChampions; }
        public long getTotalDamageTaken() { return totalDamageTaken; }
        public void setTotalDamageTaken(long totalDamageTaken) { this.totalDamageTaken = totalDamageTaken; }
        public long getVisionScore() { return visionScore; }
        public void setVisionScore(long visionScore) { this.visionScore = visionScore; }
        public int getWardsPlaced() { return wardsPlaced; }
        public void setWardsPlaced(int wardsPlaced) { this.wardsPlaced = wardsPlaced; }
        public int getWardsKilled() { return wardsKilled; }
        public void setWardsKilled(int wardsKilled) { this.wardsKilled = wardsKilled; }
        public boolean getFirstBloodKill() { return firstBloodKill; }
        public void setFirstBloodKill(boolean firstBloodKill) { this.firstBloodKill = firstBloodKill; }
        public boolean getFirstTowerKill() { return firstTowerKill; }
        public void setFirstTowerKill(boolean firstTowerKill) { this.firstTowerKill = firstTowerKill; }
        public int getChampLevel() { return champLevel; }
        public void setChampLevel(int champLevel) { this.champLevel = champLevel; }
        public int getItem0() { return item0; }
        public void setItem0(int item0) { this.item0 = item0; }
        public int getItem1() { return item1; }
        public void setItem1(int item1) { this.item1 = item1; }
        public int getItem2() { return item2; }
        public void setItem2(int item2) { this.item2 = item2; }
        public int getItem3() { return item3; }
        public void setItem3(int item3) { this.item3 = item3; }
        public int getItem4() { return item4; }
        public void setItem4(int item4) { this.item4 = item4; }
        public int getItem5() { return item5; }
        public void setItem5(int item5) { this.item5 = item5; }
        public int getItem6() { return item6; }
        public void setItem6(int item6) { this.item6 = item6; }
        public int getSummoner1Id() { return summoner1Id; }
        public void setSummoner1Id(int summoner1Id) { this.summoner1Id = summoner1Id; }
        public int getSummoner2Id() { return summoner2Id; }
        public void setSummoner2Id(int summoner2Id) { this.summoner2Id = summoner2Id; }
        public int getPrimaryStyle() { return primaryStyle; }
        public void setPrimaryStyle(int primaryStyle) { this.primaryStyle = primaryStyle; }
        public int getSubStyle() { return subStyle; }
        public void setSubStyle(int subStyle) { this.subStyle = subStyle; }
    }
}