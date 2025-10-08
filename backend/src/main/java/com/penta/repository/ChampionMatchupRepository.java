package com.penta.repository;

import com.penta.model.Champion;
import com.penta.model.ChampionMatchup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionMatchupRepository extends JpaRepository<ChampionMatchup, Long> {
    
    Optional<ChampionMatchup> findByChampion1AndChampion2AndPatchAndRankAndRole(
            Champion champion1, Champion champion2, String patch, String rank, String role);
    
    List<ChampionMatchup> findByChampion1AndPatchAndRankAndRole(
            Champion champion1, String patch, String rank, String role);
    
    List<ChampionMatchup> findByChampion2AndPatchAndRankAndRole(
            Champion champion2, String patch, String rank, String role);
    
    @Query("SELECT cm FROM ChampionMatchup cm WHERE cm.champion1 = :champion AND cm.patch = :patch AND cm.rank = :rank AND cm.role = :role ORDER BY cm.matchupScore DESC")
    List<ChampionMatchup> findBestMatchupsForChampion(@Param("champion") Champion champion, 
                                                      @Param("patch") String patch, 
                                                      @Param("rank") String rank, 
                                                      @Param("role") String role);
    
    @Query("SELECT cm FROM ChampionMatchup cm WHERE cm.champion2 = :champion AND cm.patch = :patch AND cm.rank = :rank AND cm.role = :role ORDER BY cm.matchupScore ASC")
    List<ChampionMatchup> findWorstMatchupsForChampion(@Param("champion") Champion champion, 
                                                       @Param("patch") String patch, 
                                                       @Param("rank") String rank, 
                                                       @Param("role") String role);
    
    @Query("SELECT cm FROM ChampionMatchup cm WHERE (cm.champion1 = :champion1 AND cm.champion2 = :champion2) OR (cm.champion1 = :champion2 AND cm.champion2 = :champion1) AND cm.patch = :patch AND cm.rank = :rank")
    List<ChampionMatchup> findMatchupBetweenChampions(@Param("champion1") Champion champion1, 
                                                      @Param("champion2") Champion champion2, 
                                                      @Param("patch") String patch, 
                                                      @Param("rank") String rank);
}
