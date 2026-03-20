package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.CreateMatchContributionRequest;
import com.dbeast.cricket.dto.MatchContributionResponse;
import com.dbeast.cricket.dto.MatchExpenseDiscountResponse;
import com.dbeast.cricket.dto.MatchExpenseParticipantResponse;
import com.dbeast.cricket.dto.MatchExpenseResponse;
import com.dbeast.cricket.dto.MatchFinanceOverviewResponse;
import com.dbeast.cricket.dto.MatchFinancePlayerResponse;
import com.dbeast.cricket.dto.MatchFinanceSummaryResponse;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchAvailability;
import com.dbeast.cricket.entity.MatchContribution;
import com.dbeast.cricket.entity.MatchExpense;
import com.dbeast.cricket.entity.MatchStatus;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.repository.MatchAvailabilityRepository;
import com.dbeast.cricket.repository.MatchContributionRepository;
import com.dbeast.cricket.repository.MatchExpenseRepository;
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchFinanceService {

    private final MatchRepository matchRepository;
    private final MatchAvailabilityRepository matchAvailabilityRepository;
    private final MatchExpenseRepository matchExpenseRepository;
    private final MatchContributionRepository matchContributionRepository;
    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final MatchWalletSettlementService matchWalletSettlementService;

    public MatchFinanceService(
            MatchRepository matchRepository,
            MatchAvailabilityRepository matchAvailabilityRepository,
            MatchExpenseRepository matchExpenseRepository,
            MatchContributionRepository matchContributionRepository,
            PlayerRepository playerRepository,
            PlayerTeamRepository playerTeamRepository,
            MatchWalletSettlementService matchWalletSettlementService
    ) {
        this.matchRepository = matchRepository;
        this.matchAvailabilityRepository = matchAvailabilityRepository;
        this.matchExpenseRepository = matchExpenseRepository;
        this.matchContributionRepository = matchContributionRepository;
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
        this.matchWalletSettlementService = matchWalletSettlementService;
    }

    public MatchFinanceOverviewResponse getMatchFinanceOverview(Long matchId, String playerMobile) {
        Match match = findMatch(matchId);
        Player viewer = findPlayerByMobile(playerMobile);

        validatePlayerBelongsToMatch(viewer, match);

        List<MatchExpense> expenses = matchExpenseRepository.findByMatchIdOrderByExpenseDateAscIdAsc(matchId);
        List<MatchContribution> contributions = matchContributionRepository.findByMatchIdOrderByContributionDateAscIdAsc(matchId);

        Map<Long, Double> contributionAmounts = contributions.stream()
                .collect(Collectors.toMap(
                        contribution -> contribution.getPlayer().getId(),
                        MatchContribution::getAmount,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        Map<Long, Double> payableAmounts = calculatePayables(expenses);

        List<MatchFinancePlayerResponse> players = getMatchPlayers(match, expenses, contributions).stream()
                .map(player -> mapFinancePlayer(
                        player,
                        match,
                        payableAmounts.getOrDefault(player.getId(), 0.0),
                        contributionAmounts.get(player.getId())
                ))
                .toList();

        List<MatchExpenseResponse> expenseResponses = expenses.stream()
                .map(this::mapExpense)
                .toList();

        List<MatchContributionResponse> contributionResponses = contributions.stream()
                .map(this::mapContribution)
                .toList();

        double totalExpenses = expenses.stream().mapToDouble(MatchExpense::getTotalAmount).sum();
        double totalContributions = contributions.stream().mapToDouble(MatchContribution::getAmount).sum();

        return new MatchFinanceOverviewResponse(
                new MatchFinanceSummaryResponse(
                        roundToTwoDecimals(totalExpenses),
                        roundToTwoDecimals(totalContributions),
                        roundToTwoDecimals(totalContributions - totalExpenses),
                        expenseResponses.size(),
                        (int) contributions.stream().filter(contribution -> contribution.getAmount() > 0).count()
                ),
                expenseResponses,
                contributionResponses,
                players
        );
    }

    @Transactional
    public MatchContributionResponse upsertContribution(
            Long matchId,
            String recorderMobile,
            CreateMatchContributionRequest request
    ) {
        Match match = findMatch(matchId);
        Player recorder = findPlayerByMobile(recorderMobile);
        Player targetPlayer = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        validateCaptainCanManageFinance(recorder, match);
        validateMatchCompleted(match);
        validatePlayerBelongsToMatch(targetPlayer, match);

        MatchContribution contribution = matchContributionRepository
                .findByMatchIdAndPlayerId(matchId, request.getPlayerId())
                .orElseGet(() -> {
                    MatchContribution newContribution = new MatchContribution();
                    newContribution.setMatch(match);
                    newContribution.setPlayer(targetPlayer);
                    return newContribution;
                });

        double previousAmount = contribution.getAmount() == null ? 0.0 : contribution.getAmount();
        double nextAmount = roundToTwoDecimals(request.getAmount());

        contribution.setRecordedBy(recorder);
        contribution.setAmount(nextAmount);
        contribution.setContributionDate(LocalDate.now());

        MatchContribution savedContribution = matchContributionRepository.save(contribution);
        matchWalletSettlementService.synchronizeMatchWallets(matchId);
        return mapContribution(savedContribution);
    }

    @Transactional
    public MatchFinanceOverviewResponse recalculateMatchFinance(Long matchId, String captainMobile) {
        Match match = findMatch(matchId);
        Player captain = findPlayerByMobile(captainMobile);

        validateCaptainCanManageFinance(captain, match);
        validateMatchCompleted(match);

        matchWalletSettlementService.synchronizeMatchWallets(matchId);
        return getMatchFinanceOverview(matchId, captainMobile);
    }

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
    }

    private Player findPlayerByMobile(String mobile) {
        return playerRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private void validateMatchCompleted(Match match) {
        if (getStatus(match) != MatchStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Finance entries can only be added after the match is marked completed"
            );
        }
    }

    private void validatePlayerBelongsToMatch(Player player, Match match) {
        Set<String> playerTeams = playerTeamRepository.findByPlayer(player)
                .stream()
                .map(PlayerTeam::getTeam)
                .map(Team::getTeamName)
                .collect(Collectors.toSet());

        if (!playerTeams.contains(match.getTeamA()) && !playerTeams.contains(match.getTeamB())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Player is not part of this match");
        }
    }

    private void validateCaptainCanManageFinance(Player player, Match match) {
        Set<String> captainTeams = playerTeamRepository.findByPlayer(player)
                .stream()
                .filter(playerTeam -> playerTeam.getRole() == TeamMemberRole.CAPTAIN)
                .map(PlayerTeam::getTeam)
                .map(Team::getTeamName)
                .collect(Collectors.toSet());

        if (!captainTeams.contains(match.getTeamA()) && !captainTeams.contains(match.getTeamB())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a captain of this match can manage finance"
            );
        }
    }

    private List<Player> getMatchPlayers(
            Match match,
            List<MatchExpense> expenses,
            List<MatchContribution> contributions
    ) {
        Map<Long, Player> players = new LinkedHashMap<>();

        matchAvailabilityRepository.findByMatchId(match.getId()).stream()
                .filter(MatchAvailability::isAvailable)
                .map(MatchAvailability::getPlayer)
                .forEach(player -> players.putIfAbsent(player.getId(), player));

        expenses.stream()
                .flatMap(expense -> expense.getParticipants().stream())
                .map(participant -> participant.getPlayer())
                .forEach(player -> players.putIfAbsent(player.getId(), player));

        contributions.stream()
                .map(MatchContribution::getPlayer)
                .forEach(player -> players.putIfAbsent(player.getId(), player));

        return List.copyOf(players.values());
    }

    private MatchExpenseResponse mapExpense(MatchExpense expense) {
        Map<Long, Double> discountByPlayer = expense.getDiscounts().stream()
                .collect(Collectors.toMap(
                        discount -> discount.getPlayer().getId(),
                        discount -> discount.getAmount()
                ));

        return new MatchExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getCategory(),
                expense.isMandatoryForAvailablePlayers(),
                expense.getTotalAmount(),
                expense.getSplitCount(),
                expense.getPerPlayerAmount(),
                expense.getExpenseDate(),
                expense.getCreatedBy().getName(),
                expense.getParticipants().stream()
                        .map(participant -> new MatchExpenseParticipantResponse(
                                participant.getPlayer().getId(),
                                participant.getPlayer().getName(),
                                participant.getPlayer().getTeams().stream()
                                        .map(PlayerTeam::getTeam)
                                        .map(Team::getTeamName)
                                        .findFirst()
                                        .orElse(""),
                                Math.max(
                                        0.0,
                                        roundToTwoDecimals(
                                                expense.getPerPlayerAmount() - discountByPlayer.getOrDefault(participant.getPlayer().getId(), 0.0)
                                        )
                                )
                        ))
                        .toList(),
                expense.getDiscounts().stream()
                        .map(discount -> new MatchExpenseDiscountResponse(
                                discount.getPlayer().getId(),
                                discount.getPlayer().getName(),
                                discount.getAmount()
                        ))
                        .toList()
        );
    }

    private MatchContributionResponse mapContribution(MatchContribution contribution) {
        return new MatchContributionResponse(
                contribution.getId(),
                contribution.getPlayer().getId(),
                contribution.getPlayer().getName(),
                contribution.getAmount(),
                contribution.getContributionDate(),
                contribution.getRecordedBy().getName()
        );
    }

    private MatchFinancePlayerResponse mapFinancePlayer(
            Player player,
            Match match,
            Double payableAmount,
            Double contributionAmount
    ) {
        PlayerTeam matchMembership = findRelevantMembership(player, match);
        double contribution = contributionAmount == null ? 0.0 : contributionAmount;
        double payable = payableAmount == null ? 0.0 : payableAmount;

        return new MatchFinancePlayerResponse(
                player.getId(),
                player.getName(),
                player.getMobile(),
                matchMembership == null ? "" : matchMembership.getTeam().getTeamName(),
                player.getPlayerRole(),
                matchMembership == null ? null : matchMembership.getRole(),
                player.getWallet() == null ? 0.0 : player.getWallet().getBalance(),
                roundToTwoDecimals(payable),
                roundToTwoDecimals(contribution),
                roundToTwoDecimals(contribution - payable)
        );
    }

    private Map<Long, Double> calculatePayables(List<MatchExpense> expenses) {
        Map<Long, Double> payables = new LinkedHashMap<>();

        for (MatchExpense expense : expenses) {
            Map<Long, Double> discountByPlayer = expense.getDiscounts().stream()
                    .collect(Collectors.toMap(
                            discount -> discount.getPlayer().getId(),
                            discount -> discount.getAmount()
                    ));

            expense.getParticipants().forEach(participant -> {
                long playerId = participant.getPlayer().getId();
                double discount = discountByPlayer.getOrDefault(playerId, 0.0);
                double netPayable = Math.max(0.0, roundToTwoDecimals(expense.getPerPlayerAmount() - discount));
                payables.merge(playerId, netPayable, Double::sum);
            });
        }

        return payables.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> roundToTwoDecimals(entry.getValue()),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private PlayerTeam findRelevantMembership(Player player, Match match) {
        return playerTeamRepository.findByPlayer(player).stream()
                .filter(playerTeam -> {
                    String teamName = playerTeam.getTeam().getTeamName();
                    return teamName.equals(match.getTeamA()) || teamName.equals(match.getTeamB());
                })
                .findFirst()
                .orElse(null);
    }

    private MatchStatus getStatus(Match match) {
        return match.getStatus() == null ? MatchStatus.SCHEDULED : match.getStatus();
    }
}
