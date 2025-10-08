package com.penta.repository;

import com.penta.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    Optional<Match> findByMatchId(String matchId);
    
    List<Match> findByGameStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Match> findByGameMode(String gameMode);
    
    List<Match> findByQueueId(Integer queueId);
    
    List<Match> findByGameVersion(String gameVersion);
    
    @Query("SELECT m FROM Match m WHERE m.gameStartTime >= :since ORDER BY m.gameStartTime DESC")
    List<Match> findRecentMatches(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.gameStartTime >= :since")
    Long countMatchesSince(@Param("since") LocalDateTime since);
}
