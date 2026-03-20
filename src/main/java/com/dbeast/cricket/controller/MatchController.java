package com.dbeast.cricket.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dbeast.cricket.dto.MatchRequest;
import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.dto.NextMatchSquadResponse;
import com.dbeast.cricket.service.MatchService;

import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:5173")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    // -------------------------
    // Create Match
    // -------------------------
    @PostMapping(
        consumes = "application/json",
        produces = "application/json"
    )
    public ResponseEntity<MatchResponse> createMatch(
            Principal principal,
            @Valid @RequestBody MatchRequest request) {

        return ResponseEntity.ok(matchService.createMatch(principal.getName(), request));
    }

    // -------------------------
    // Get All Matches
    // -------------------------
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<MatchResponse>> getAllMatches(
            @RequestParam Long playerId) {

        return ResponseEntity.ok(matchService.getAllMatches(playerId));
    }

    @GetMapping(value = "/next-squad", produces = "application/json")
    public ResponseEntity<NextMatchSquadResponse> getNextMatchSquad(Principal principal) {
        return ResponseEntity.ok(matchService.getNextMatchSquad(principal.getName()));
    }

    // -------------------------
    // Update Availability
    // -------------------------
    @PostMapping("/{matchId}/availability")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long matchId,
            @RequestParam Long playerId,
            @RequestParam boolean available,
            Principal principal) {

        matchService.updateAvailability(matchId, playerId, available, principal.getName());

        return ResponseEntity.ok().build();
    }
}
