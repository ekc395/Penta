package com.penta.repository;

import com.penta.model.Player;
import com.penta.model.PlayerMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerMatchRepository extends JpaRepository<PlayerMatch, Long> {
    List<PlayerMatch> findByPlayerOrderByGameStartTimeDesc(Player player);

    Optional<PlayerMatch> findByPlayerAndMatchId(Player player, String matchId);
}