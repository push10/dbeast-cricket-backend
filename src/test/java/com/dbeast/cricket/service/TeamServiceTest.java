package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.TeamResponse;
import com.dbeast.cricket.dto.PlayerSummaryResponse;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import com.dbeast.cricket.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    void getMyTeamsReturnsAllTeamsForCurrentCaptain() {
        Player captain = new Player();
        captain.setId(1L);
        captain.setMobile("9990001111");

        Team falcons = createTeam(10L, "Falcons");
        Team titans = createTeam(11L, "Titans");

        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, falcons, TeamMemberRole.CAPTAIN),
                createMembership(captain, titans, TeamMemberRole.CAPTAIN)
        ));
        when(playerTeamRepository.findByTeam(falcons)).thenReturn(List.of(
                createMembership(captain, falcons, TeamMemberRole.CAPTAIN)
        ));
        when(playerTeamRepository.findByTeam(titans)).thenReturn(List.of(
                createMembership(captain, titans, TeamMemberRole.CAPTAIN)
        ));

        List<TeamResponse> response = teamService.getMyTeams(captain.getMobile());

        assertEquals(2, response.size());
        assertEquals("Falcons", response.get(0).getTeamName());
        assertEquals("Titans", response.get(1).getTeamName());
    }

    @Test
    void getAvailablePlayersForTeamExcludesExistingMembers() {
        Player captain = new Player();
        captain.setId(1L);
        captain.setMobile("9990001111");
        captain.setName("Captain");

        Player existingMember = new Player();
        existingMember.setId(2L);
        existingMember.setMobile("9990002222");
        existingMember.setName("Existing");

        Player availablePlayer = new Player();
        availablePlayer.setId(3L);
        availablePlayer.setMobile("9990003333");
        availablePlayer.setName("Available");

        Team falcons = createTeam(10L, "Falcons");

        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(teamRepository.findById(falcons.getId())).thenReturn(Optional.of(falcons));
        when(playerTeamRepository.existsByPlayerAndTeamAndRole(captain, falcons, TeamMemberRole.CAPTAIN)).thenReturn(true);
        when(playerTeamRepository.findByTeam(falcons)).thenReturn(List.of(
                createMembership(captain, falcons, TeamMemberRole.CAPTAIN),
                createMembership(existingMember, falcons, TeamMemberRole.MEMBER)
        ));
        when(playerRepository.findAll()).thenReturn(List.of(captain, existingMember, availablePlayer));

        List<PlayerSummaryResponse> response = teamService.getAvailablePlayersForTeam(captain.getMobile(), falcons.getId());

        assertEquals(1, response.size());
        assertEquals(availablePlayer.getId(), response.get(0).getId());
        assertNull(response.get(0).getTeamRole());
    }

    private Team createTeam(Long id, String teamName) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(teamName);
        return team;
    }

    private PlayerTeam createMembership(Player player, Team team, TeamMemberRole role) {
        PlayerTeam membership = new PlayerTeam();
        membership.setPlayer(player);
        membership.setTeam(team);
        membership.setRole(role);
        return membership;
    }
}
