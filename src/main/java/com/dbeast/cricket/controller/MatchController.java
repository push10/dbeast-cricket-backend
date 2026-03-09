package com.dbeast.cricket.controller;
 
import com.dbeast.cricket.service.MatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.Player;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        Match savedMatch = matchService.createMatch(match);
        return ResponseEntity.ok(savedMatch);
    }

    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        List<Match> matches = matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }
// Mark availability
  // Mark availability correctly through service
    @PostMapping("/{matchId}/availability")
    public ResponseEntity<Match> markAvailability(
            @PathVariable Long matchId,
            @RequestParam Long playerId,
            @RequestParam boolean available) {

        Match updatedMatch = matchService.updatePlayerAvailability(matchId, playerId, available);
        return ResponseEntity.ok(updatedMatch);
    }
}