package com.dbeast.cricket.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dbeast.cricket.dto.MatchRequest;
import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.service.MatchService;

import jakarta.validation.Valid;

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
            @Valid @RequestBody MatchRequest request) {

        return ResponseEntity.ok(matchService.createMatch(request));
    }

    // -------------------------
    // Get All Matches
    // -------------------------
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<MatchResponse>> getAllMatches(
            @RequestParam Long playerId) {

        return ResponseEntity.ok(matchService.getAllMatches(playerId));
    }

    // -------------------------
    // Update Availability
    // -------------------------
    @PostMapping("/{matchId}/availability")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long matchId,
            @RequestParam Long playerId,
            @RequestParam boolean available) {

        matchService.updateAvailability(matchId, playerId, available);

        return ResponseEntity.ok().build();
    }
}