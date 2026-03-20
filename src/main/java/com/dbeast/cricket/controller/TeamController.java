package com.dbeast.cricket.controller;

import com.dbeast.cricket.dto.AddPlayerToTeamRequest;
import com.dbeast.cricket.dto.CreateTeamRequest;
import com.dbeast.cricket.dto.PlayerSummaryResponse;
import com.dbeast.cricket.dto.TeamResponse;
import com.dbeast.cricket.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            Principal principal,
            @Valid @RequestBody CreateTeamRequest request
    ) {
        return ResponseEntity.ok(teamService.createTeam(principal.getName(), request.getTeamName()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TeamResponse>> getMyTeams(Principal principal) {
        return ResponseEntity.ok(teamService.getMyTeams(principal.getName()));
    }

    @PostMapping("/{teamId}/players")
    public ResponseEntity<TeamResponse> addPlayerToTeam(
            Principal principal,
            @PathVariable Long teamId,
            @Valid @RequestBody AddPlayerToTeamRequest request
    ) {
        return ResponseEntity.ok(teamService.addPlayerToTeam(principal.getName(), teamId, request.getMobile()));
    }

    @GetMapping("/{teamId}/available-players")
    public ResponseEntity<List<PlayerSummaryResponse>> getAvailablePlayersForTeam(
            Principal principal,
            @PathVariable Long teamId
    ) {
        return ResponseEntity.ok(teamService.getAvailablePlayersForTeam(principal.getName(), teamId));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeam(teamId));
    }
}
