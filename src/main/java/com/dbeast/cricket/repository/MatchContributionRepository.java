package com.dbeast.cricket.repository;

import com.dbeast.cricket.entity.MatchContribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchContributionRepository extends JpaRepository<MatchContribution, Long> {

    List<MatchContribution> findByMatchIdOrderByContributionDateAscIdAsc(Long matchId);

    Optional<MatchContribution> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}
