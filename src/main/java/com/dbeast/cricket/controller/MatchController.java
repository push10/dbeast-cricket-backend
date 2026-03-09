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
@CrossOrigin(origins = "http://localhost:5173") // 🔥 important for React
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping(
    consumes = "application/json",
    produces = "application/json"
    )
    public ResponseEntity<MatchResponse> createMatch(
            @Valid @RequestBody MatchRequest request) {

        return ResponseEntity.ok(matchService.createMatch(request));
    }

    // ✅ ADD THIS
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<MatchResponse>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }
}