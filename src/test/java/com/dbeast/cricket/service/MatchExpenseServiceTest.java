package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.CreateMatchExpenseRequest;
import com.dbeast.cricket.dto.MatchExpenseDiscountRequest;
import com.dbeast.cricket.dto.MatchExpenseResponse;
import com.dbeast.cricket.entity.MatchAvailability;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchExpense;
import com.dbeast.cricket.entity.MatchStatus;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerRole;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.entity.Wallet;
import com.dbeast.cricket.repository.MatchAvailabilityRepository;
import com.dbeast.cricket.repository.MatchExpenseRepository;
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchExpenseServiceTest {

    @Mock
    private MatchExpenseRepository matchExpenseRepository;

    @Mock
    private MatchAvailabilityRepository matchAvailabilityRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @Mock
    private MatchWalletSettlementService matchWalletSettlementService;

    @InjectMocks
    private MatchExpenseService matchExpenseService;

    private Match completedMatch;
    private Player captain;
    private Player member;

    @BeforeEach
    void setUp() {
        completedMatch = new Match();
        setMatchId(completedMatch, 12L);
        completedMatch.setTeamA("Falcons");
       completedMatch.setTeamB("Titans");
        completedMatch.setMatchDate(LocalDate.now());
        completedMatch.setStatus(MatchStatus.COMPLETED);

        captain = createPlayer(1L, "9990001111", "Captain");
        member = createPlayer(2L, "9990002222", "Member");
    }

    @Test
    void createMandatoryExpenseAllowsCaptainForCompletedMatch() {
        CreateMatchExpenseRequest request = new CreateMatchExpenseRequest();
        request.setTitle("Match Fees");
        request.setCategory("MATCH_FEE");
        request.setMandatoryForAvailablePlayers(true);
        request.setTotalAmount(500.0);

        when(matchRepository.findById(completedMatch.getId())).thenReturn(Optional.of(completedMatch));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)
        ));
        when(matchAvailabilityRepository.findByMatchId(completedMatch.getId())).thenReturn(List.of(
                createAvailability(completedMatch, captain, true),
                createAvailability(completedMatch, member, true)
        ));
        when(matchExpenseRepository.save(any(MatchExpense.class))).thenAnswer(invocation -> {
            MatchExpense saved = invocation.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        MatchExpenseResponse response = matchExpenseService.createExpense(
                completedMatch.getId(),
                captain.getMobile(),
                request
        );

        assertEquals("MATCH_FEE", response.getCategory());
        assertEquals(250.0, response.getPerPlayerAmount());
        assertEquals(2, response.getParticipants().size());
        assertEquals("Captain", response.getCreatedByName());
    }

    @Test
    void createOptionalExpenseAppliesDiscount() {
        CreateMatchExpenseRequest request = new CreateMatchExpenseRequest();
        request.setTitle("Tea");
        request.setCategory("TEA");
        request.setMandatoryForAvailablePlayers(false);
        request.setTotalAmount(100.0);
        request.setParticipantPlayerIds(List.of(member.getId()));

        MatchExpenseDiscountRequest discount = new MatchExpenseDiscountRequest();
        discount.setPlayerId(member.getId());
        discount.setAmount(60.0);
        discount.setDescription("Player paid travel separately");
        request.setDiscounts(List.of(discount));

        when(matchRepository.findById(completedMatch.getId())).thenReturn(Optional.of(completedMatch));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)
        ));
        when(matchAvailabilityRepository.findByMatchId(completedMatch.getId())).thenReturn(List.of(
                createAvailability(completedMatch, captain, true),
                createAvailability(completedMatch, member, true)
        ));
        when(playerRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(matchExpenseRepository.save(any(MatchExpense.class))).thenAnswer(invocation -> {
            MatchExpense saved = invocation.getArgument(0);
            saved.setId(60L);
            return saved;
        });

        MatchExpenseResponse response = matchExpenseService.createExpense(
                completedMatch.getId(),
                captain.getMobile(),
                request
        );

        assertEquals(1, response.getParticipants().size());
        assertEquals(40.0, response.getParticipants().get(0).getNetPayable());
        assertEquals("Player paid travel separately", response.getDiscounts().get(0).getDescription());
        assertEquals(completedMatch.getId(), response.getDiscounts().get(0).getMatchId());
    }

    @Test
    void createMandatoryExpenseAppliesDiscountToWalletBalance() {
        CreateMatchExpenseRequest request = new CreateMatchExpenseRequest();
        request.setTitle("Match Fees");
        request.setCategory("MATCH_FEE");
        request.setMandatoryForAvailablePlayers(true);
        request.setTotalAmount(500.0);

        MatchExpenseDiscountRequest discount = new MatchExpenseDiscountRequest();
        discount.setPlayerId(member.getId());
        discount.setAmount(60.0);
        request.setDiscounts(List.of(discount));

        when(matchRepository.findById(completedMatch.getId())).thenReturn(Optional.of(completedMatch));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)
        ));
        when(matchAvailabilityRepository.findByMatchId(completedMatch.getId())).thenReturn(List.of(
                createAvailability(completedMatch, captain, true),
                createAvailability(completedMatch, member, true)
        ));
        when(playerRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(matchExpenseRepository.save(any(MatchExpense.class))).thenAnswer(invocation -> {
            MatchExpense saved = invocation.getArgument(0);
            saved.setId(61L);
            return saved;
        });

        MatchExpenseResponse response = matchExpenseService.createExpense(
                completedMatch.getId(),
                captain.getMobile(),
                request
        );

        assertEquals(250.0, response.getPerPlayerAmount());
        assertEquals(190.0, response.getParticipants().stream()
                .filter(participant -> participant.getPlayerId().equals(member.getId()))
                .findFirst()
                .orElseThrow()
                .getNetPayable());
    }

    @Test
    void createExpenseRejectsFutureMatch() {
        completedMatch.setMatchDate(LocalDate.now().plusDays(1));
        completedMatch.setStatus(MatchStatus.SCHEDULED);

        CreateMatchExpenseRequest request = new CreateMatchExpenseRequest();
        request.setTitle("Tea");
        request.setCategory("TEA");
        request.setMandatoryForAvailablePlayers(false);
        request.setTotalAmount(105.0);
        request.setParticipantPlayerIds(List.of(member.getId()));

        when(matchRepository.findById(completedMatch.getId())).thenReturn(Optional.of(completedMatch));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, "Falcons", TeamMemberRole.CAPTAIN)
        ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchExpenseService.createExpense(completedMatch.getId(), captain.getMobile(), request)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void getMatchExpensesRejectsPlayerOutsideMatch() {
        when(matchRepository.findById(completedMatch.getId())).thenReturn(Optional.of(completedMatch));
        when(playerRepository.findByMobile(member.getMobile())).thenReturn(Optional.of(member));
        when(playerTeamRepository.findByPlayer(member)).thenReturn(List.of(
                createMembership(member, "Warriors", TeamMemberRole.MEMBER)
        ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchExpenseService.getMatchExpenses(completedMatch.getId(), member.getMobile())
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    private Player createPlayer(Long id, String mobile, String name) {
        Player player = new Player();
        player.setId(id);
        player.setMobile(mobile);
        player.setName(name);
        player.setPlayerRole(PlayerRole.ALLROUNDER);

        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);
        player.setWallet(wallet);
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
