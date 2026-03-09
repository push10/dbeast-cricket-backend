package com.dbeast.cricket.service;
 
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.PlayerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.Player;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public Match createMatch(Match match) {
        match.setAvailableCount(0); // initially 0 players available
        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match updatePlayerAvailability(Long matchId, Long playerId, boolean available) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (available) {
            match.getAvailablePlayers().add(player);
        } else {
            match.getAvailablePlayers().remove(player);
        }

        match.setAvailableCount(match.getAvailablePlayers().size());
        return matchRepository.save(match);
    }
}