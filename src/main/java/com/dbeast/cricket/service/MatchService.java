package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.MatchRequest;
import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.dto.NextMatchSquadResponse;
import com.dbeast.cricket.dto.SquadPlayerResponse;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchAvailability;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.repository.MatchAvailabilityRepository;
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import com.dbeast.cricket.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchAvailabilityRepository availabilityRepository;
    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final TeamRepository teamRepository;

    public MatchService(MatchRepository matchRepository,
                        MatchAvailabilityRepository availabilityRepository,
                        PlayerRepository playerRepository,
                        PlayerTeamRepository playerTeamRepository,
                        TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.availabilityRepository = availabilityRepository;
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
        this.teamRepository = teamRepository;
    }

    // -------------------------
    // Create Match
    // -------------------------
    public MatchResponse createMatch(String creatorMobile, MatchRequest request) {

        Match match = mapToEntity(creatorMobile, request);

        Match savedMatch = matchRepository.save(match);

        return mapToResponse(savedMatch, null);
    }

    // -------------------------
    // Get All Matches
    // -------------------------
    public List<MatchResponse> getAllMatches(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        Set<String> playerTeamNames = playerTeamRepository.findByPlayer(player)
                .stream()
                .map(PlayerTeam::getTeam)
                .map(Team::getTeamName)
                .collect(Collectors.toSet());

        if (playerTeamNames.isEmpty()) {
            return List.of();
        }

        return matchRepository.findAll()
                .stream()
                .filter(match -> !match.getMatchDate().isBefore(LocalDate.now()))
                .filter(match -> playerTeamNames.contains(match.getTeamA()) || playerTeamNames.contains(match.getTeamB()))
                .sorted((left, right) -> left.getMatchDate().compareTo(right.getMatchDate()))
                .map(match -> mapToResponse(match, playerId))
                .collect(Collectors.toList());
    }

    public NextMatchSquadResponse getNextMatchSquad(String playerMobile) {
        Player player = playerRepository.findByMobile(playerMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        Set<String> playerTeamNames = playerTeamRepository.findByPlayer(player)
                .stream()
                .map(PlayerTeam::getTeam)
                .map(Team::getTeamName)
                .collect(Collectors.toSet());

        if (playerTeamNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No squad available for this player");
        }

        Match nextMatch = matchRepository.findAll()
                .stream()
                .filter(match -> !match.getMatchDate().isBefore(LocalDate.now()))
                .filter(match -> playerTeamNames.contains(match.getTeamA()) || playerTeamNames.contains(match.getTeamB()))
                .sorted((left, right) -> left.getMatchDate().compareTo(right.getMatchDate()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No squad available for this player"));

        List<SquadPlayerResponse> squad = availabilityRepository.findByMatchId(nextMatch.getId())
                .stream()
                .filter(MatchAvailability::isAvailable)
                .map(MatchAvailability::getPlayer)
                .map(squadPlayer -> new SquadPlayerResponse(
                        squadPlayer.getId(),
                        squadPlayer.getName(),
                        squadPlayer.getMobile(),
                        squadPlayer.getPlayerRole()
                ))
                .toList();

        return new NextMatchSquadResponse(
                nextMatch.getId(),
                nextMatch.getTeamA(),
                nextMatch.getTeamB(),
                nextMatch.getMatchDate(),
                squad.size(),
                squad
        );
    }

    // -------------------------
    // Get Match By Id
    // -------------------------
    public MatchResponse getMatchById(Long id, Long playerId) {

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        return mapToResponse(match, playerId);
    }

    // -------------------------
    // Update Availability
    // -------------------------
    public void updateAvailability(Long matchId, Long playerId, boolean available, String actorMobile) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        Player targetPlayer = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        Player actor = playerRepository.findByMobile(actorMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        validateAvailabilityUpdate(actor, targetPlayer, match, available);

        MatchAvailability availability =
                availabilityRepository
                        .findByMatchIdAndPlayerId(matchId, playerId)
                        .orElseGet(() -> {
                            MatchAvailability newAvailability = new MatchAvailability();
                            newAvailability.setMatch(match);
                            newAvailability.setPlayer(targetPlayer);
                            return newAvailability;
                        });

        availability.setAvailable(available);

        availabilityRepository.save(availability);
    }

    // ------------------------
    // Private Mapper Methods
    // ------------------------

    private Match mapToEntity(String creatorMobile, MatchRequest request) {
        Match match = new Match();
        match.setTeamA(resolveHomeTeamName(creatorMobile, request));
        match.setTeamB(request.getTeamB().trim());
        match.setMatchDate(request.getMatchDate());
        return match;
    }

    private String resolveHomeTeamName(String creatorMobile, MatchRequest request) {
        if (request.getTeamId() != null) {
            Player captain = playerRepository.findByMobile(creatorMobile)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

            if (!playerTeamRepository.existsByPlayerAndTeamAndRole(captain, team, TeamMemberRole.CAPTAIN)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only the captain of the selected team can create this match"
                );
            }

            return team.getTeamName();
        }

        if (request.getTeamA() == null || request.getTeamA().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team A name is required");
        }

        return request.getTeamA().trim();
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

    private void validateAvailabilityUpdate(Player actor, Player targetPlayer, Match match, boolean available) {
        if (actor.getId().equals(targetPlayer.getId())) {
            return;
        }

        if (available) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Players can only mark themselves available"
            );
        }

        if (!canCaptainManageAvailability(actor, targetPlayer, match)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the captain can mark a selected player unavailable"
            );
        }
    }

    private boolean canCaptainManageAvailability(Player actor, Player targetPlayer, Match match) {
        Set<String> matchTeams = Set.of(match.getTeamA(), match.getTeamB());

        Set<String> captainTeams = playerTeamRepository.findByPlayer(actor)
                .stream()
                .filter(playerTeam -> playerTeam.getRole() == TeamMemberRole.CAPTAIN)
                .map(PlayerTeam::getTeam)
                .map(team -> team.getTeamName())
                .filter(matchTeams::contains)
                .collect(Collectors.toSet());

        if (captainTeams.isEmpty()) {
            return false;
        }

        return playerTeamRepository.findByPlayer(targetPlayer)
                .stream()
                .map(PlayerTeam::getTeam)
                .map(team -> team.getTeamName())
                .anyMatch(captainTeams::contains);
    }
}
