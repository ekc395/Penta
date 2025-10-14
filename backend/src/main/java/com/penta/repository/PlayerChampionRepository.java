package com.penta.repository;

import com.penta.model.Player;
import com.penta.model.PlayerChampion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.penta.model.Champion;
import java.util.Optional;
import java.util.List;

@Repository
public interface PlayerChampionRepository extends JpaRepository<PlayerChampion, Long> {
    List<PlayerChampion> findByPlayerOrderByGamesPlayedDesc(Player player);
    Optional<PlayerChampion> findByPlayerAndChampion(Player player, Champion champion);
}

