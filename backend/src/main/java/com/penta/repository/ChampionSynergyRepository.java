package com.penta.repository;

import com.penta.model.Champion;
import com.penta.model.ChampionSynergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionSynergyRepository extends JpaRepository<ChampionSynergy, Long> {
    
    Optional<ChampionSynergy> findByChampion1AndChampion2AndPatchAndRank(
            Champion champion1, Champion champion2, String patch, String rank);
    
    List<ChampionSynergy> findByChampion1AndPatchAndRank(
            Champion champion1, String patch, String rank);
    
    List<ChampionSynergy> findByChampion2AndPatchAndRank(
            Champion champion2, String patch, String rank);
    
    @Query("SELECT cs FROM ChampionSynergy cs WHERE cs.champion1 = :champion AND cs.patch = :patch AND cs.rank = :rank ORDER BY cs.synergyScore DESC")
    List<ChampionSynergy> findBestSynergiesForChampion(@Param("champion") Champion champion, 
                                                       @Param("patch") String patch, 
                                                       @Param("rank") String rank);
    
    @Query("SELECT cs FROM ChampionSynergy cs WHERE cs.champion2 = :champion AND cs.patch = :patch AND cs.rank = :rank ORDER BY cs.synergyScore DESC")
    List<ChampionSynergy> findBestSynergiesForChampion2(@Param("champion") Champion champion, 
                                                        @Param("patch") String patch, 
                                                        @Param("rank") String rank);
    
    @Query("SELECT cs FROM ChampionSynergy cs WHERE (cs.champion1 = :champion1 AND cs.champion2 = :champion2) OR (cs.champion1 = :champion2 AND cs.champion2 = :champion1) AND cs.patch = :patch AND cs.rank = :rank")
    List<ChampionSynergy> findSynergyBetweenChampions(@Param("champion1") Champion champion1, 
                                                      @Param("champion2") Champion champion2, 
                                                      @Param("patch") String patch, 
                                                      @Param("rank") String rank);
    
    @Query("SELECT cs FROM ChampionSynergy cs WHERE cs.synergyType = :synergyType AND cs.patch = :patch AND cs.rank = :rank ORDER BY cs.synergyScore DESC")
    List<ChampionSynergy> findTopSynergiesByType(@Param("synergyType") String synergyType, 
                                                 @Param("patch") String patch, 
                                                 @Param("rank") String rank);
}
