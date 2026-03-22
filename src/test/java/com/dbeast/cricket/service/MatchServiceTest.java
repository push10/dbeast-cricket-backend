package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.MatchResponse;
import com.dbeast.cricket.dto.NextMatchSquadResponse;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchAvailability;
import com.dbeast.cricket.entity.MatchStatus;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerRole;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.repository.MatchAvailabilityRepository;
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import com.dbeast.cricket.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchAvailabilityRepository availabilityRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private MatchService matchService;

    private Match upcomingMatch;
    private Player captain;
    private Player selectedPlayer;

    @BeforeEach
    void setUp() {
        upcomingMatch = new Match();
        setMatchId(upcomingMatch, 10L);
        upcomingMatch.setTeamA("Falcons");
        upcomingMatch.setTeamB("Titans");
        upcomingMatch.setMatchDate(LocalDate.now().plusDays(1));
        upcomingMatch.setStatus(MatchStatus.SCHEDULED);

        captain = createPlayer(1L, "9990001111", "Captain", PlayerRole.ALLROUNDER);
        selectedPlayer = createPlayer(2L, "9990002222", "Selected Player", PlayerRole.BATSMAN);
    }

    @Test
    void getNextMatchSquadReturnsUpcomingAvailablePlayers() {
        Match laterMatch = new Match();
        setMatchId(laterMatch, 20L);
        laterMatch.setTeamA("Falcons");
        laterMatch.setTeamB("Warriors");
        laterMatch.setMatchDate(LocalDate.now().plusDays(5));

        Player unavailablePlayer = createPlayer(3L, "9990003333", "Unavailable", PlayerRole.BOWLER);

        when(playerRepository.findByMobile(selectedPlayer.getMobile())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer))
                .thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));
        when(matchRepository.findAll()).thenReturn(List.of(laterMatch, upcomingMatch));
        when(availabilityRepository.findByMatchId(upcomingMatch.getId())).thenReturn(List.of(
                createAvailability(upcomingMatch, selectedPlayer, true),
                createAvailability(upcomingMatch, unavailablePlayer, false)
        ));

        NextMatchSquadResponse response = matchService.getNextMatchSquad(selectedPlayer.getMobile());

        assertEquals(upcomingMatch.getId(), response.getMatchId());
        assertEquals(1, response.getAvailableCount());
        assertEquals(1, response.getSquad().size());
        assertEquals(selectedPlayer.getId(), response.getSquad().get(0).getId());
    }

    @Test
    void getNextMatchSquadReturnsOnlyRelevantTeamMatch() {
        Match unrelatedMatch = new Match();
        setMatchId(unrelatedMatch, 21L);
        unrelatedMatch.setTeamA("Warriors");
        unrelatedMatch.setTeamB("Knights");
        unrelatedMatch.setMatchDate(LocalDate.now().plusDays(1));

        when(playerRepository.findByMobile(selectedPlayer.getMobile())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer))
                .thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));
        when(matchRepository.findAll()).thenReturn(List.of(unrelatedMatch, upcomingMatch));
        when(availabilityRepository.findByMatchId(upcomingMatch.getId())).thenReturn(List.of());

        NextMatchSquadResponse response = matchService.getNextMatchSquad(selectedPlayer.getMobile());

        assertEquals(upcomingMatch.getId(), response.getMatchId());
    }

    @Test
    void getNextMatchSquadRejectsPlayerWithoutTeamMembership() {
        when(playerRepository.findByMobile(selectedPlayer.getMobile())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchService.getNextMatchSquad(selectedPlayer.getMobile())
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void getAllMatchesExcludesPastMatches() {
        Match pastMatch = new Match();
        setMatchId(pastMatch, 5L);
        pastMatch.setTeamA("Falcons");
        pastMatch.setTeamB("Legends");
        pastMatch.setMatchDate(LocalDate.now().minusDays(1));

        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer))
                .thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));
        when(matchRepository.findAll()).thenReturn(List.of(upcomingMatch, pastMatch));
        when(availabilityRepository.countByMatchIdAndAvailableTrue(upcomingMatch.getId())).thenReturn(0L);

        List<MatchResponse> response = matchService.getAllMatches(selectedPlayer.getId());

        assertEquals(1, response.size());
        assertTrue(response.stream().allMatch(match -> !match.getMatchDate().isBefore(LocalDate.now())));
    }

    @Test
    void getAllMatchesReturnsOnlyMatchesForPlayersTeams() {
        Match unrelatedMatch = new Match();
        setMatchId(unrelatedMatch, 11L);
        unrelatedMatch.setTeamA("Warriors");
        unrelatedMatch.setTeamB("Knights");
        unrelatedMatch.setMatchDate(LocalDate.now().plusDays(2));

        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer))
                .thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));
        when(matchRepository.findAll()).thenReturn(List.of(unrelatedMatch, upcomingMatch));
        when(availabilityRepository.countByMatchIdAndAvailableTrue(upcomingMatch.getId())).thenReturn(0L);

        List<MatchResponse> response = matchService.getAllMatches(selectedPlayer.getId());

        assertEquals(1, response.size());
        assertEquals(upcomingMatch.getId(), response.get(0).getId());
    }

    @Test
    void getAllMatchesReturnsEmptyForPlayerWithoutTeamMembership() {
        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer)).thenReturn(List.of());

        List<MatchResponse> response = matchService.getAllMatches(selectedPlayer.getId());

        assertTrue(response.isEmpty());
    }

    @Test
    void getCompletedMatchesReturnsOnlyCompletedMatchesForPlayersTeams() {
        Match futureMatch = new Match();
        setMatchId(futureMatch, 22L);
        futureMatch.setTeamA("Falcons");
        futureMatch.setTeamB("Titans");
        futureMatch.setMatchDate(LocalDate.now().plusDays(2));

        Match completedMatch = new Match();
        setMatchId(completedMatch, 23L);
        completedMatch.setTeamA("Falcons");
        completedMatch.setTeamB("Knights");
        completedMatch.setMatchDate(LocalDate.now());
        completedMatch.setStatus(MatchStatus.COMPLETED);

        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer))
                .thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));
        when(matchRepository.findAll()).thenReturn(List.of(futureMatch, completedMatch));
        when(availabilityRepository.countByMatchIdAndAvailableTrue(completedMatch.getId())).thenReturn(0L);

        List<MatchResponse> response = matchService.getCompletedMatches(selectedPlayer.getId());

        assertEquals(1, response.size());
        assertEquals(completedMatch.getId(), response.get(0).getId());
    }

    @Test
    void getMatchPlayersReturnsRosterWithAvailability() {
        Player opponentCaptain = createPlayer(3L, "9990003333", "Opponent Captain", PlayerRole.BOWLER);
        Player opponentPlayer = createPlayer(4L, "9990004444", "Opponent Player", PlayerRole.WICKET_KEEPER);

        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain))
                .thenReturn(List.of(createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)));
        when(playerTeamRepository.findAll()).thenReturn(List.of(
                createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN),
                createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER),
                createMembership(opponentCaptain, "Titans", TeamMemberRole.CAPTAIN),
                createMembership(opponentPlayer, "Titans", TeamMemberRole.MEMBER)
        ));
        when(availabilityRepository.findByMatchId(upcomingMatch.getId())).thenReturn(List.of(
                createAvailability(upcomingMatch, captain, true),
                createAvailability(upcomingMatch, selectedPlayer, false),
                createAvailability(upcomingMatch, opponentPlayer, true)
        ));

        var response = matchService.getMatchPlayers(upcomingMatch.getId(), captain.getMobile());

        assertEquals(4, response.size());
        assertEquals(captain.getId(), response.get(0).getId());
        assertTrue(response.get(0).isAvailable());
        assertEquals("Falcons", response.get(0).getTeamName());
        assertEquals(selectedPlayer.getId(), response.get(1).getId());
        assertFalse(response.get(1).isAvailable());
        assertEquals("Titans", response.get(2).getTeamName());
        assertTrue(response.get(3).isAvailable());
    }

    @Test
    void getMatchPlayersRejectsPlayerOutsideMatch() {
        Player outsider = createPlayer(5L, "9990005555", "Outsider", PlayerRole.BOWLER);

        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findByMobile(outsider.getMobile())).thenReturn(Optional.of(outsider));
        when(playerTeamRepository.findByPlayer(outsider))
                .thenReturn(List.of(createMembership(outsider, "Warriors", TeamMemberRole.MEMBER)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchService.getMatchPlayers(upcomingMatch.getId(), outsider.getMobile())
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void markMatchCompletedAllowsCaptainForTodayMatch() {
        upcomingMatch.setMatchDate(LocalDate.now());

        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain))
                .thenReturn(List.of(createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(availabilityRepository.countByMatchIdAndAvailableTrue(upcomingMatch.getId())).thenReturn(0L);
        when(availabilityRepository.findByMatchIdAndPlayerId(upcomingMatch.getId(), captain.getId()))
                .thenReturn(Optional.empty());

        MatchResponse response = matchService.markMatchCompleted(upcomingMatch.getId(), captain.getMobile());

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(MatchStatus.COMPLETED, upcomingMatch.getStatus());
    }

    @Test
    void markMatchCompletedRejectsNonCaptain() {
        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findByMobile(selectedPlayer.getMobile())).thenReturn(Optional.of(selectedPlayer));
        when(playerTeamRepository.findByPlayer(selectedPlayer))
                .thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchService.markMatchCompleted(upcomingMatch.getId(), selectedPlayer.getMobile())
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void createMatchUsesSelectedCaptainTeam() {
        Team falcons = new Team();
        falcons.setId(10L);
        falcons.setTeamName("Falcons");

        com.dbeast.cricket.dto.MatchRequest request = new com.dbeast.cricket.dto.MatchRequest();
        request.setTeamId(falcons.getId());
        request.setTeamB("Titans");
        request.setMatchDate(LocalDate.now().plusDays(2));

        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(teamRepository.findById(falcons.getId())).thenReturn(Optional.of(falcons));
        when(playerTeamRepository.existsByPlayerAndTeamAndRole(captain, falcons, TeamMemberRole.CAPTAIN)).thenReturn(true);
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match savedMatch = invocation.getArgument(0);
            setMatchId(savedMatch, 99L);
            return savedMatch;
        });
        when(availabilityRepository.countByMatchIdAndAvailableTrue(99L)).thenReturn(0L);

        MatchResponse response = matchService.createMatch(captain.getMobile(), request);

        assertEquals("Falcons", response.getTeamA());
        assertEquals("Titans", response.getTeamB());
    }

    @Test
    void createMatchRejectsSelectedTeamWhenUserIsNotCaptain() {
        Player teammate = createPlayer(4L, "9990004444", "Teammate", PlayerRole.BOWLER);
        Team falcons = new Team();
        falcons.setId(10L);
        falcons.setTeamName("Falcons");

        com.dbeast.cricket.dto.MatchRequest request = new com.dbeast.cricket.dto.MatchRequest();
        request.setTeamId(falcons.getId());
        request.setTeamB("Titans");
        request.setMatchDate(LocalDate.now().plusDays(2));

        when(playerRepository.findByMobile(teammate.getMobile())).thenReturn(Optional.of(teammate));
        when(teamRepository.findById(falcons.getId())).thenReturn(Optional.of(falcons));
        when(playerTeamRepository.existsByPlayerAndTeamAndRole(teammate, falcons, TeamMemberRole.CAPTAIN)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchService.createMatch(teammate.getMobile(), request)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void updateAvailabilityAllowsPlayerToMarkSelfUnavailable() {
        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerRepository.findByMobile(selectedPlayer.getMobile())).thenReturn(Optional.of(selectedPlayer));
        when(availabilityRepository.findByMatchIdAndPlayerId(upcomingMatch.getId(), selectedPlayer.getId()))
                .thenReturn(Optional.empty());

        matchService.updateAvailability(
                upcomingMatch.getId(),
                selectedPlayer.getId(),
                false,
                selectedPlayer.getMobile()
        );

        ArgumentCaptor<MatchAvailability> availabilityCaptor = ArgumentCaptor.forClass(MatchAvailability.class);
        verify(availabilityRepository).save(availabilityCaptor.capture());
        assertFalse(availabilityCaptor.getValue().isAvailable());
        assertEquals(selectedPlayer.getId(), availabilityCaptor.getValue().getPlayer().getId());
    }

    @Test
    void updateAvailabilityAllowsCaptainToMarkTeamMemberUnavailable() {
        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(availabilityRepository.findByMatchIdAndPlayerId(upcomingMatch.getId(), selectedPlayer.getId()))
                .thenReturn(Optional.empty());
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)));
        when(playerTeamRepository.findByPlayer(selectedPlayer)).thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));

        matchService.updateAvailability(
                upcomingMatch.getId(),
                selectedPlayer.getId(),
                false,
                captain.getMobile()
        );

        verify(availabilityRepository).save(any(MatchAvailability.class));
    }

    @Test
    void updateAvailabilityAllowsCaptainToMarkTeamMemberAvailable() {
        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(availabilityRepository.findByMatchIdAndPlayerId(upcomingMatch.getId(), selectedPlayer.getId()))
                .thenReturn(Optional.empty());
        when(playerTeamRepository.findByPlayer(captain))
                .thenReturn(List.of(createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)));
        when(playerTeamRepository.findByPlayer(selectedPlayer))
                .thenReturn(List.of(createMembership(selectedPlayer, "Falcons", TeamMemberRole.MEMBER)));

        matchService.updateAvailability(
                upcomingMatch.getId(),
                selectedPlayer.getId(),
                true,
                captain.getMobile()
        );

        ArgumentCaptor<MatchAvailability> availabilityCaptor = ArgumentCaptor.forClass(MatchAvailability.class);
        verify(availabilityRepository).save(availabilityCaptor.capture());
        assertTrue(availabilityCaptor.getValue().isAvailable());
        assertEquals(selectedPlayer.getId(), availabilityCaptor.getValue().getPlayer().getId());
    }

    @Test
    void updateAvailabilityRejectsNonCaptainMarkingAnotherPlayerUnavailable() {
        Player teammate = createPlayer(4L, "9990004444", "Teammate", PlayerRole.BOWLER);

        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerRepository.findByMobile(teammate.getMobile())).thenReturn(Optional.of(teammate));
        when(playerTeamRepository.findByPlayer(teammate)).thenReturn(List.of(createMembership(teammate, "Falcons", TeamMemberRole.MEMBER)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchService.updateAvailability(
                        upcomingMatch.getId(),
                        selectedPlayer.getId(),
                        false,
                        teammate.getMobile()
                )
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(availabilityRepository, never()).save(any(MatchAvailability.class));
    }

    @Test
    void updateAvailabilityRejectsNonCaptainMarkingAnotherPlayerAvailable() {
        Player teammate = createPlayer(4L, "9990004444", "Teammate", PlayerRole.BOWLER);

        when(matchRepository.findById(upcomingMatch.getId())).thenReturn(Optional.of(upcomingMatch));
        when(playerRepository.findById(selectedPlayer.getId())).thenReturn(Optional.of(selectedPlayer));
        when(playerRepository.findByMobile(teammate.getMobile())).thenReturn(Optional.of(teammate));
        when(playerTeamRepository.findByPlayer(teammate))
                .thenReturn(List.of(createMembership(teammate, "Falcons", TeamMemberRole.MEMBER)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchService.updateAvailability(
                        upcomingMatch.getId(),
                        selectedPlayer.getId(),
                        true,
                        teammate.getMobile()
                )
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(availabilityRepository, never()).save(any(MatchAvailability.class));
    }

    private Player createPlayer(Long id, String mobile, String name, PlayerRole role) {
        Player player = new Player();
        player.setId(id);
        player.setMobile(mobile);
        player.setName(name);
        player.setPlayerRole(role);
        return player;
    }

    private MatchAvailability createAvailability(Match match, Player player, boolean available) {
        MatchAvailability availability = new MatchAvailability();
        availability.setMatch(match);
        availability.setPlayer(player);
        availability.setAvailable(available);
        return availability;
    }

    private PlayerTeam createMembership(Player player, String teamName, TeamMemberRole role) {
        Team team = new Team();
        team.setTeamName(teamName);

        PlayerTeam membership = new PlayerTeam();
        membership.setPlayer(player);
        membership.setTeam(team);
        membership.setRole(role);
        return membership;
    }

    private void setMatchId(Match match, Long id) {
        try {
            Field field = Match.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(match, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to assign match id in test setup", exception);
        }
    }
}
