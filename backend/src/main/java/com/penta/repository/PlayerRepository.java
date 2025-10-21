package com.penta.repository;

import com.penta.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findBySummonerName(String summonerName);
    
    Optional<Player> findByPuuid(String puuid);
    
    Optional<Player> findBySummonerId(String summonerId);
    
    List<Player> findByRegion(String region);
    
    List<Player> findBySummonerLevelGreaterThan(Integer level);
    
    @Query("SELECT p FROM Player p WHERE p.lastUpdated >= :since ORDER BY p.lastUpdated DESC")
    List<Player> findRecentlyUpdated(@Param("since") LocalDateTime since);
    
    @Query("SELECT p FROM Player p WHERE p.region = :region AND p.summonerLevel >= :minLevel ORDER BY p.summonerLevel DESC")
    List<Player> findTopPlayersByRegion(@Param("region") String region, @Param("minLevel") Integer minLevel);
    
    @Query("SELECT p FROM Player p WHERE p.lastAccessed IS NULL OR p.lastAccessed < :threshold")
    List<Player> findStalePlayersBefore(@Param("threshold") LocalDateTime threshold);
}
