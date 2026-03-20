package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.PlayerSummaryResponse;
import com.dbeast.cricket.dto.TeamResponse;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.entity.UserRole;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import com.dbeast.cricket.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;

    public TeamService(
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            PlayerTeamRepository playerTeamRepository
    ) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
    }

    @Transactional
    public TeamResponse createTeam(String captainMobile, String teamName) {
        Player captain = findPlayerByMobile(captainMobile);
        String normalizedTeamName = teamName.trim();

        Team team = new Team();
        team.setTeamName(normalizedTeamName);
        team = teamRepository.save(team);

        PlayerTeam membership = new PlayerTeam();
        membership.setPlayer(captain);
        membership.setTeam(team);
        membership.setRole(TeamMemberRole.CAPTAIN);
        playerTeamRepository.save(membership);

        captain.setUserRole(UserRole.CAPTAIN);
        playerRepository.save(captain);

        return mapTeam(team);
    }

    @Transactional
    public TeamResponse addPlayerToTeam(String captainMobile, Long teamId, String playerMobile) {
        Player captain = findPlayerByMobile(captainMobile);
        Team team = findTeam(teamId);

        if (!playerTeamRepository.existsByPlayerAndTeamAndRole(captain, team, TeamMemberRole.CAPTAIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only captain can add players to this team");
        }

        Player player = findPlayerByMobile(playerMobile.trim());

        if (playerTeamRepository.existsByPlayerAndTeam(player, team)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Player is already in this team");
        }

        PlayerTeam membership = new PlayerTeam();
        membership.setPlayer(player);
        membership.setTeam(team);
        membership.setRole(TeamMemberRole.MEMBER);
        playerTeamRepository.save(membership);

        return mapTeam(team);
    }

    public TeamResponse getTeam(Long teamId) {
        return mapTeam(findTeam(teamId));
    }

    public List<TeamResponse> getMyTeams(String mobile) {
        Player player = findPlayerByMobile(mobile);

        return playerTeamRepository.findByPlayer(player).stream()
                .map(PlayerTeam::getTeam)
                .distinct()
                .map(this::mapTeam)
                .toList();
    }

    public List<PlayerSummaryResponse> getAvailablePlayersForTeam(String captainMobile, Long teamId) {
        Player captain = findPlayerByMobile(captainMobile);
        Team team = findTeam(teamId);

        if (!playerTeamRepository.existsByPlayerAndTeamAndRole(captain, team, TeamMemberRole.CAPTAIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only captain can view available players for this team");
        }

        Set<Long> existingPlayerIds = playerTeamRepository.findByTeam(team).stream()
                .map(PlayerTeam::getPlayer)
                .map(Player::getId)
                .collect(Collectors.toSet());

        return playerRepository.findAll().stream()
                .filter(player -> !existingPlayerIds.contains(player.getId()))
                .sorted((left, right) -> {
                    String leftName = left.getName() == null ? "" : left.getName();
                    String rightName = right.getName() == null ? "" : right.getName();
                    return leftName.compareToIgnoreCase(rightName);
                })
                .map(player -> new PlayerSummaryResponse(
                        player.getId(),
                        player.getName(),
                        player.getMobile(),
                        player.getEmail(),
                        player.getUserRole(),
                        player.getPlayerRole(),
                        null
                ))
                .toList();
    }

    private Player findPlayerByMobile(String mobile) {
        return playerRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    private TeamResponse mapTeam(Team team) {
        List<PlayerSummaryResponse> players = playerTeamRepository.findByTeam(team).stream()
                .map(this::mapPlayerSummary)
                .toList();

        return new TeamResponse(team.getId(), team.getTeamName(), players);
    }

    private PlayerSummaryResponse mapPlayerSummary(PlayerTeam playerTeam) {
        Player player = playerTeam.getPlayer();

        return new PlayerSummaryResponse(
                player.getId(),
                player.getName(),
                player.getMobile(),
                player.getEmail(),
                player.getUserRole(),
                player.getPlayerRole(),
                playerTeam.getRole()
        );
    }
}
