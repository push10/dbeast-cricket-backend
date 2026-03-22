package com.dbeast.cricket.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dbeast.cricket.dto.MatchRequest;
import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.dto.CreateMatchContributionRequest;
import com.dbeast.cricket.dto.CreateMatchExpenseRequest;
import com.dbeast.cricket.dto.MatchContributionResponse;
import com.dbeast.cricket.dto.MatchExpenseResponse;
import com.dbeast.cricket.dto.MatchFinanceOverviewResponse;
import com.dbeast.cricket.dto.MatchAvailabilityPlayerResponse;
import com.dbeast.cricket.dto.NextMatchSquadResponse;
import com.dbeast.cricket.service.MatchExpenseService;
import com.dbeast.cricket.service.MatchFinanceService;
import com.dbeast.cricket.service.MatchService;

import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:5173")
public class MatchController {

    private final MatchService matchService;
    private final MatchExpenseService matchExpenseService;
    private final MatchFinanceService matchFinanceService;

    public MatchController(
            MatchService matchService,
            MatchExpenseService matchExpenseService,
            MatchFinanceService matchFinanceService
    ) {
        this.matchService = matchService;
        this.matchExpenseService = matchExpenseService;
        this.matchFinanceService = matchFinanceService;
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

    @GetMapping(value = "/completed", produces = "application/json")
    public ResponseEntity<List<MatchResponse>> getCompletedMatches(
            @RequestParam Long playerId) {

        return ResponseEntity.ok(matchService.getCompletedMatches(playerId));
    }

    @GetMapping(value = "/next-squad", produces = "application/json")
    public ResponseEntity<NextMatchSquadResponse> getNextMatchSquad(Principal principal) {
        return ResponseEntity.ok(matchService.getNextMatchSquad(principal.getName()));
    }

    @PostMapping(value = "/{matchId}/complete", produces = "application/json")
    public ResponseEntity<MatchResponse> markMatchCompleted(
            @PathVariable Long matchId,
            Principal principal) {

        return ResponseEntity.ok(matchService.markMatchCompleted(matchId, principal.getName()));
    }

    @GetMapping(value = "/{matchId}/expenses", produces = "application/json")
    public ResponseEntity<List<MatchExpenseResponse>> getMatchExpenses(
            @PathVariable Long matchId,
            Principal principal) {

        return ResponseEntity.ok(matchExpenseService.getMatchExpenses(matchId, principal.getName()));
    }

    @GetMapping(value = "/{matchId}/finance", produces = "application/json")
    public ResponseEntity<MatchFinanceOverviewResponse> getMatchFinanceOverview(
            @PathVariable Long matchId,
            Principal principal) {

        return ResponseEntity.ok(matchFinanceService.getMatchFinanceOverview(matchId, principal.getName()));
    }

    @PostMapping(value = "/{matchId}/finance/recalculate", produces = "application/json")
    public ResponseEntity<MatchFinanceOverviewResponse> recalculateMatchFinance(
            @PathVariable Long matchId,
            Principal principal) {

        return ResponseEntity.ok(matchFinanceService.recalculateMatchFinance(matchId, principal.getName()));
    }

    @PostMapping(value = "/{matchId}/expenses", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MatchExpenseResponse> createMatchExpense(
            @PathVariable Long matchId,
            Principal principal,
            @Valid @RequestBody CreateMatchExpenseRequest request) {

        return ResponseEntity.ok(matchExpenseService.createExpense(matchId, principal.getName(), request));
    }

    @PostMapping(value = "/{matchId}/contributions", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MatchContributionResponse> upsertMatchContribution(
            @PathVariable Long matchId,
            Principal principal,
            @Valid @RequestBody CreateMatchContributionRequest request) {

        return ResponseEntity.ok(matchFinanceService.upsertContribution(matchId, principal.getName(), request));
    }

    @GetMapping(value = "/{matchId}/players", produces = "application/json")
    public ResponseEntity<List<MatchAvailabilityPlayerResponse>> getMatchPlayers(
            @PathVariable Long matchId,
            Principal principal) {

        return ResponseEntity.ok(matchService.getMatchPlayers(matchId, principal.getName()));
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
