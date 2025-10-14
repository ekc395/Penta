package com.penta.service;

import com.penta.dto.*;
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
                player.setProfileIconUrl(String.format("https://ddragon.leagueoflegends.com/cdn/14.20.1/img/profileicon/%d.png", 
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
     * Get summoner profile (lightweight DTO)
     */
    public Optional<SummonerProfileDto> getSummonerProfile(String summonerName, String region) {
        Optional<Player> playerOpt = getPlayerBySummonerName(summonerName, region);
        if (playerOpt.isEmpty()) {
            return Optional.empty();
        }

        Player player = playerOpt.get();
        SummonerProfileDto dto = new SummonerProfileDto();
        dto.setSummonerName(player.getSummonerName());
        dto.setRegion(player.getRegion());
        dto.setSummonerId(player.getSummonerId());
        dto.setPuuid(player.getPuuid());
        dto.setSummonerLevel(player.getSummonerLevel());
        dto.setProfileIconUrl(player.getProfileIconUrl());

        return Optional.of(dto);
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
        participant.setFirstBloodKill(dto.isFirstBloodKill());
        participant.setFirstTowerKill(dto.isFirstTowerKill());
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
        champion.setImageUrl(String.format("https://ddragon.leagueoflegends.com/cdn/14.20.1/img/champion/%s.png", 
                championDto.getId()));
        champion.setSplashUrl(String.format("https://ddragon.leagueoflegends.com/cdn/img/champion/splash/%s_0.jpg", 
                championDto.getId()));
        champion.setTags(String.join(",", championDto.getTags()));
        

        // Set default values for required fields
        champion.setLane("ALL");
        champion.setRole("ALL");
        
        return champion;
    }
}