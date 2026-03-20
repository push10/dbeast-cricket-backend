package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.CreateMatchContributionRequest;
import com.dbeast.cricket.dto.MatchContributionResponse;
import com.dbeast.cricket.dto.MatchFinanceOverviewResponse;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchAvailability;
import com.dbeast.cricket.entity.MatchContribution;
import com.dbeast.cricket.entity.MatchExpenseDiscount;
import com.dbeast.cricket.entity.MatchExpense;
import com.dbeast.cricket.entity.MatchExpenseParticipant;
import com.dbeast.cricket.entity.MatchStatus;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerRole;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.entity.Wallet;
import com.dbeast.cricket.repository.MatchAvailabilityRepository;
import com.dbeast.cricket.repository.MatchContributionRepository;
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
class MatchFinanceServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchExpenseRepository matchExpenseRepository;

    @Mock
    private MatchAvailabilityRepository matchAvailabilityRepository;

    @Mock
    private MatchContributionRepository matchContributionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @Mock
    private MatchWalletSettlementService matchWalletSettlementService;

    @InjectMocks
    private MatchFinanceService matchFinanceService;

    private Match match;
    private Player captain;
    private Player member;
    private Team falcons;
    @BeforeEach
    void setUp() {
        match = new Match();
        setMatchId(match, 31L);
        match.setTeamA("Falcons");
        match.setTeamB("Titans");
        match.setMatchDate(LocalDate.now());
        match.setStatus(MatchStatus.COMPLETED);

        falcons = new Team();
        falcons.setId(1L);
        falcons.setTeamName("Falcons");

        captain = createPlayer(1L, "9990001111", "Captain", 50.0);
        member = createPlayer(2L, "9990002222", "Member", 10.0);
    }

    @Test
    void upsertContributionSynchronizesMatchWallets() {
        CreateMatchContributionRequest request = new CreateMatchContributionRequest();
        request.setPlayerId(member.getId());
        request.setAmount(125.0);

        MatchContribution existingContribution = new MatchContribution();
        existingContribution.setId(70L);
        existingContribution.setMatch(match);
        existingContribution.setPlayer(member);
        existingContribution.setRecordedBy(captain);
        existingContribution.setAmount(25.0);
        existingContribution.setContributionDate(LocalDate.now().minusDays(1));

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, falcons, TeamMemberRole.CAPTAIN)
        ));
        when(playerTeamRepository.findByPlayer(member)).thenReturn(List.of(
                createMembership(member, falcons, TeamMemberRole.MEMBER)
        ));
        when(matchContributionRepository.findByMatchIdAndPlayerId(match.getId(), member.getId()))
                .thenReturn(Optional.of(existingContribution));
        when(matchContributionRepository.save(any(MatchContribution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchContributionResponse response = matchFinanceService.upsertContribution(
                match.getId(),
                captain.getMobile(),
                request
        );

        assertEquals(125.0, response.getAmount());
    }

    @Test
    void recalculateMatchFinanceAllowsCaptainAndReturnsOverview() {
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, falcons, TeamMemberRole.CAPTAIN)
        ));
        when(matchExpenseRepository.findByMatchIdOrderByExpenseDateAscIdAsc(match.getId())).thenReturn(List.of());
        when(matchContributionRepository.findByMatchIdOrderByContributionDateAscIdAsc(match.getId())).thenReturn(List.of());
        when(matchAvailabilityRepository.findByMatchId(match.getId())).thenReturn(List.of());

        MatchFinanceOverviewResponse response = matchFinanceService.recalculateMatchFinance(
                match.getId(),
                captain.getMobile()
        );

        assertEquals(0.0, response.getSummary().getTotalExpenses());
        assertEquals(0.0, response.getSummary().getTotalContributions());
    }

    @Test
    void getMatchFinanceOverviewAggregatesExpensesAndContributions() {
        MatchExpense expense = new MatchExpense();
        expense.setId(100L);
        expense.setMatch(match);
        expense.setCreatedBy(captain);
        expense.setTitle("Tea");
        expense.setCategory("TEA");
        expense.setMandatoryForAvailablePlayers(false);
        expense.setTotalAmount(15.0);
        expense.setSplitCount(1);
        expense.setPerPlayerAmount(15.0);
        expense.setExpenseDate(LocalDate.now());
        expense.setParticipants(List.of(createExpenseParticipant(expense, member)));
        expense.setDiscounts(List.of(createExpenseDiscount(expense, member, 5.0)));

        MatchContribution contribution = new MatchContribution();
        contribution.setId(200L);
        contribution.setMatch(match);
        contribution.setPlayer(member);
        contribution.setRecordedBy(captain);
        contribution.setAmount(125.0);
        contribution.setContributionDate(LocalDate.now());

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(playerRepository.findByMobile(member.getMobile())).thenReturn(Optional.of(member));
        when(playerTeamRepository.findByPlayer(member)).thenReturn(List.of(
                createMembership(member, falcons, TeamMemberRole.MEMBER)
        ));
        when(matchAvailabilityRepository.findByMatchId(match.getId())).thenReturn(List.of(
                createAvailability(match, captain, true),
                createAvailability(match, member, true)
        ));
        when(playerTeamRepository.findByPlayer(captain)).thenReturn(List.of(
                createMembership(captain, falcons, TeamMemberRole.CAPTAIN)
        ));
        when(playerTeamRepository.findByPlayer(member)).thenReturn(List.of(
                createMembership(member, falcons, TeamMemberRole.MEMBER)
        ));
        when(matchExpenseRepository.findByMatchIdOrderByExpenseDateAscIdAsc(match.getId())).thenReturn(List.of(expense));
        when(matchContributionRepository.findByMatchIdOrderByContributionDateAscIdAsc(match.getId())).thenReturn(List.of(contribution));

        MatchFinanceOverviewResponse response = matchFinanceService.getMatchFinanceOverview(match.getId(), member.getMobile());

        assertEquals(15.0, response.getSummary().getTotalExpenses());
        assertEquals(125.0, response.getSummary().getTotalContributions());
        assertEquals(110.0, response.getSummary().getBalanceDifference());
        assertEquals(2, response.getPlayers().size());
        assertEquals(10.0, response.getPlayers().stream()
                .filter(player -> player.getPlayerId().equals(member.getId()))
                .findFirst()
                .orElseThrow()
                .getPayableAmount());
    }

    @Test
    void upsertContributionRejectsCaptainOutsideMatch() {
        Player outsideCaptain = createPlayer(3L, "9990003333", "Outside Captain", 0.0);

        CreateMatchContributionRequest request = new CreateMatchContributionRequest();
        request.setPlayerId(member.getId());
        request.setAmount(50.0);

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(playerRepository.findByMobile(outsideCaptain.getMobile())).thenReturn(Optional.of(outsideCaptain));
        when(playerRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(playerTeamRepository.findByPlayer(outsideCaptain)).thenReturn(List.of(
                createMembership(outsideCaptain, createTeam(2L, "Titans"), TeamMemberRole.MEMBER)
        ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchFinanceService.upsertContribution(match.getId(), outsideCaptain.getMobile(), request)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    private Player createPlayer(Long id, String mobile, String name, double walletBalance) {
        Player player = new Player();
        player.setId(id);
        player.setMobile(mobile);
        player.setName(name);
        player.setPlayerRole(PlayerRole.ALLROUNDER);

        Wallet wallet = new Wallet();
        wallet.setBalance(walletBalance);
        player.setWallet(wallet);

        return player;
    }

    private PlayerTeam createMembership(Player player, Team team, TeamMemberRole role) {
        PlayerTeam membership = new PlayerTeam();
        membership.setPlayer(player);
        membership.setTeam(team);
        membership.setRole(role);
        return membership;
    }

    private MatchExpenseParticipant createExpenseParticipant(MatchExpense expense, Player player) {
        MatchExpenseParticipant participant = new MatchExpenseParticipant();
        participant.setMatchExpense(expense);
        participant.setPlayer(player);
        return participant;
    }

    private MatchExpenseDiscount createExpenseDiscount(MatchExpense expense, Player player, double amount) {
        MatchExpenseDiscount discount = new MatchExpenseDiscount();
        discount.setMatchExpense(expense);
        discount.setPlayer(player);
        discount.setAmount(amount);
        return discount;
    }

    private MatchAvailability createAvailability(Match match, Player player, boolean available) {
        MatchAvailability availability = new MatchAvailability();
        availability.setMatch(match);
        availability.setPlayer(player);
        availability.setAvailable(available);
        return availability;
    }

    private Team createTeam(Long id, String teamName) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(teamName);
        return team;
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
