package com.penta.repository;

import com.penta.model.Champion;
import com.penta.model.ChampionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionStatsRepository extends JpaRepository<ChampionStats, Long> {
    
    Optional<ChampionStats> findByChampionAndPatchAndRankAndRole(
            Champion champion, String patch, String rank, String role);
    
    List<ChampionStats> findByChampionAndPatch(Champion champion, String patch);
    
    List<ChampionStats> findByPatchAndRankAndRole(String patch, String rank, String role);
    
    List<ChampionStats> findByRoleAndRankOrderByWinRateDesc(String role, String rank);
    
    List<ChampionStats> findByRoleAndRankOrderByPickRateDesc(String role, String rank);
    
    List<ChampionStats> findByRoleAndRankOrderByTierDesc(String role, String rank);
    
    @Query("SELECT cs FROM ChampionStats cs WHERE cs.champion = :champion AND cs.patch = :patch AND cs.rank = :rank")
    List<ChampionStats> findByChampionPatchAndRank(@Param("champion") Champion champion, 
                                                   @Param("patch") String patch, 
                                                   @Param("rank") String rank);
    
    @Query("SELECT cs FROM ChampionStats cs WHERE cs.role = :role AND cs.rank = :rank AND cs.winRate >= :minWinRate ORDER BY cs.winRate DESC")
    List<ChampionStats> findTopChampionsByRoleAndRank(@Param("role") String role, 
                                                      @Param("rank") String rank, 
                                                      @Param("minWinRate") Double minWinRate);
}
