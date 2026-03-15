package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.MatchRequest;
import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchAvailability;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.repository.MatchAvailabilityRepository;
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchAvailabilityRepository availabilityRepository;
    private final PlayerRepository playerRepository;

    public MatchService(MatchRepository matchRepository,
                        MatchAvailabilityRepository availabilityRepository,
                        PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.availabilityRepository = availabilityRepository;
        this.playerRepository = playerRepository;
    }

    // -------------------------
    // Create Match
    // -------------------------
    public MatchResponse createMatch(MatchRequest request) {

        Match match = mapToEntity(request);

        Match savedMatch = matchRepository.save(match);

        return mapToResponse(savedMatch, null);
    }

    // -------------------------
    // Get All Matches
    // -------------------------
    public List<MatchResponse> getAllMatches(Long playerId) {

        return matchRepository.findAll()
                .stream()
                .map(match -> mapToResponse(match, playerId))
                .collect(Collectors.toList());
    }

    // -------------------------
    // Get Match By Id
    // -------------------------
    public MatchResponse getMatchById(Long id, Long playerId) {

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + id));

        return mapToResponse(match, playerId);
    }

    // -------------------------
    // Update Availability
    // -------------------------
    public void updateAvailability(Long matchId, Long playerId, boolean available) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        MatchAvailability availability =
                availabilityRepository
                        .findByMatchIdAndPlayerId(matchId, playerId)
                        .orElseGet(() -> {
                            MatchAvailability newAvailability = new MatchAvailability();
                            newAvailability.setMatch(match);
                            newAvailability.setPlayer(player);
                            return newAvailability;
                        });

        availability.setAvailable(available);

        availabilityRepository.save(availability);
    }

    // ------------------------
    // Private Mapper Methods
    // ------------------------

    private Match mapToEntity(MatchRequest request) {
        Match match = new Match();
        match.setTeamA(request.getTeamA());
        match.setTeamB(request.getTeamB());
        match.setMatchDate(request.getMatchDate());
        return match;
    }

    private MatchResponse mapToResponse(Match match, Long playerId) {

        int availableCount =
                (int) availabilityRepository.countByMatchIdAndAvailableTrue(match.getId());

        Boolean myStatus = false;

        if (playerId != null) {
            myStatus = availabilityRepository
                    .findByMatchIdAndPlayerId(match.getId(), playerId)
                    .map(MatchAvailability::isAvailable)
                    .orElse(false);
        }

        return new MatchResponse(
                match.getId(),
                match.getTeamA(),
                match.getTeamB(),
                match.getMatchDate(),
                availableCount,
                myStatus
        );
    }
}