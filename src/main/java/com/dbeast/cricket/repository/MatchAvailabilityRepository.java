package com.dbeast.cricket.repository; 

import com.dbeast.cricket.entity.MatchAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchAvailabilityRepository extends JpaRepository<MatchAvailability, Long> {

    Optional<MatchAvailability> findByMatchIdAndPlayerId(Long matchId, Long playerId);

    List<MatchAvailability> findByMatchId(Long matchId);

    long countByMatchIdAndAvailableTrue(Long matchId);
}