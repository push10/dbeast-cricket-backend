package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.MatchRequest;
import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    // Create Match
    public MatchResponse createMatch(MatchRequest request) {

        Match match = mapToEntity(request);

        Match savedMatch = matchRepository.save(match);

        return mapToResponse(savedMatch);
    }

    // Get All Matches
    public List<MatchResponse> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get Match By Id
    public MatchResponse getMatchById(Long id) {

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with id: " + id));

        return mapToResponse(match);
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

    private MatchResponse mapToResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getTeamA(),
                match.getTeamB(),
                match.getMatchDate()
        );
    }
}