package com.dbeast.cricket.repository;

import com.dbeast.cricket.entity.MatchWalletImpact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchWalletImpactRepository extends JpaRepository<MatchWalletImpact, Long> {

    List<MatchWalletImpact> findByMatchId(Long matchId);

    Optional<MatchWalletImpact> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}
