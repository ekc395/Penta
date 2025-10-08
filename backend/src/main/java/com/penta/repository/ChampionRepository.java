package com.penta.repository;

import com.penta.model.Champion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampionRepository extends JpaRepository<Champion, Long> {
    
    Optional<Champion> findByChampionId(Integer championId);
    
    Optional<Champion> findByName(String name);
    
    List<Champion> findByRole(String role);
    
    List<Champion> findByLane(String lane);
    
    List<Champion> findByTier(Integer tier);
    
    @Query("SELECT c FROM Champion c WHERE c.tags LIKE %:tag%")
    List<Champion> findByTag(@Param("tag") String tag);
    
    @Query("SELECT c FROM Champion c WHERE c.winRate >= :minWinRate ORDER BY c.winRate DESC")
    List<Champion> findTopChampionsByWinRate(@Param("minWinRate") Double minWinRate);
    
    @Query("SELECT c FROM Champion c WHERE c.pickRate >= :minPickRate ORDER BY c.pickRate DESC")
    List<Champion> findTopChampionsByPickRate(@Param("minPickRate") Double minPickRate);
}
