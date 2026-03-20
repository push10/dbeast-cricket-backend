package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.CreateMatchExpenseRequest;
import com.dbeast.cricket.dto.MatchExpenseDiscountRequest;
import com.dbeast.cricket.dto.MatchExpenseDiscountResponse;
import com.dbeast.cricket.dto.MatchExpenseParticipantResponse;
import com.dbeast.cricket.dto.MatchExpenseResponse;
import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchAvailability;
import com.dbeast.cricket.entity.MatchExpenseDiscount;
import com.dbeast.cricket.entity.MatchExpense;
import com.dbeast.cricket.entity.MatchExpenseParticipant;
import com.dbeast.cricket.entity.MatchStatus;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.repository.MatchAvailabilityRepository;
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
public class MatchExpenseService {

    private static final String MATCH_FEE_CATEGORY = "MATCH_FEE";

    private final MatchExpenseRepository matchExpenseRepository;
    private final MatchAvailabilityRepository matchAvailabilityRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final MatchWalletSettlementService matchWalletSettlementService;

    public MatchExpenseService(
            MatchExpenseRepository matchExpenseRepository,
            MatchAvailabilityRepository matchAvailabilityRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            PlayerTeamRepository playerTeamRepository,
            MatchWalletSettlementService matchWalletSettlementService
    ) {
        this.matchExpenseRepository = matchExpenseRepository;
        this.matchAvailabilityRepository = matchAvailabilityRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
        this.matchWalletSettlementService = matchWalletSettlementService;
    }

    public List<MatchExpenseResponse> getMatchExpenses(Long matchId, String playerMobile) {
        Match match = findMatch(matchId);
        Player player = findPlayerByMobile(playerMobile);

        validatePlayerBelongsToMatch(player, match);

        return matchExpenseRepository.findByMatchIdOrderByExpenseDateAscIdAsc(matchId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public MatchExpenseResponse createExpense(Long matchId, String playerMobile, CreateMatchExpenseRequest request) {
        Match match = findMatch(matchId);
        Player captain = findPlayerByMobile(playerMobile);

        validateCaptainCanManageExpenses(captain, match);
        validateMatchCompleted(match);

        String normalizedCategory = normalizeCategory(request.getCategory());
        boolean mandatoryForAvailablePlayers = normalizedCategory.equals(MATCH_FEE_CATEGORY)
                || request.isMandatoryForAvailablePlayers();

        List<Player> participants = resolveParticipants(match, request, mandatoryForAvailablePlayers);
        Map<Long, Double> discounts = resolveDiscounts(request.getDiscounts(), participants);
        double perPlayerAmount = roundToTwoDecimals(request.getTotalAmount() / participants.size());

        MatchExpense expense = new MatchExpense();
        expense.setMatch(match);
        expense.setCreatedBy(captain);
        expense.setTitle(request.getTitle().trim());
        expense.setCategory(normalizedCategory);
        expense.setMandatoryForAvailablePlayers(mandatoryForAvailablePlayers);
        expense.setTotalAmount(request.getTotalAmount());
        expense.setSplitCount(participants.size());
        expense.setPerPlayerAmount(perPlayerAmount);
        expense.setExpenseDate(LocalDate.now());

        expense.setParticipants(participants.stream()
                .map(player -> {
                    MatchExpenseParticipant participant = new MatchExpenseParticipant();
                    participant.setMatchExpense(expense);
                    participant.setPlayer(player);
                    return participant;
                })
                .toList());

        expense.setDiscounts(discounts.entrySet().stream()
                .map(entry -> {
                    MatchExpenseDiscount discount = new MatchExpenseDiscount();
                    discount.setMatchExpense(expense);
                    discount.setPlayer(findPlayer(entry.getKey()));
                    discount.setAmount(entry.getValue());
                    return discount;
                })
                .toList());

        MatchExpense savedExpense = matchExpenseRepository.save(expense);
        matchWalletSettlementService.synchronizeMatchWallets(matchId);
        return mapToResponse(savedExpense);
    }

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
    }

    private Player findPlayerByMobile(String mobile) {
        return playerRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private void validatePlayerBelongsToMatch(Player player, Match match) {
        Set<String> playerTeams = playerTeamRepository.findByPlayer(player)
                .stream()
                .map(PlayerTeam::getTeam)
                .map(team -> team.getTeamName())
                .collect(java.util.stream.Collectors.toSet());

        if (!playerTeams.contains(match.getTeamA()) && !playerTeams.contains(match.getTeamB())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Player is not part of this match");
        }
    }

    private void validateCaptainCanManageExpenses(Player player, Match match) {
        Set<String> captainTeams = playerTeamRepository.findByPlayer(player)
                .stream()
                .filter(playerTeam -> playerTeam.getRole() == TeamMemberRole.CAPTAIN)
                .map(PlayerTeam::getTeam)
                .map(team -> team.getTeamName())
                .collect(java.util.stream.Collectors.toSet());

        if (!captainTeams.contains(match.getTeamA()) && !captainTeams.contains(match.getTeamB())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a captain of this match can add expenses"
            );
        }
    }

    private void validateMatchCompleted(Match match) {
        if (getStatus(match) != MatchStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expenses can only be added after the match is marked completed"
            );
        }
    }

    private List<Player> resolveParticipants(Match match, CreateMatchExpenseRequest request, boolean mandatory) {
        List<Player> availablePlayers = matchAvailabilityRepository.findByMatchId(match.getId())
                .stream()
                .filter(MatchAvailability::isAvailable)
                .map(MatchAvailability::getPlayer)
                .toList();

        if (mandatory) {
            if (availablePlayers.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Mandatory expenses require available players for the match"
                );
            }

            return availablePlayers;
        }

        List<Long> participantIds = request.getParticipantPlayerIds();
        if (participantIds == null || participantIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Optional expenses require selected participant players"
            );
        }

        Map<Long, Player> availablePlayersById = availablePlayers.stream()
                .collect(Collectors.toMap(Player::getId, player -> player));

        return participantIds.stream()
                .distinct()
                .map(playerId -> {
                    Player participant = availablePlayersById.get(playerId);
                    if (participant == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Optional expense participants must be marked available for the match"
                        );
                    }
                    return participant;
                })
                .toList();
    }

    private Map<Long, Double> resolveDiscounts(List<MatchExpenseDiscountRequest> discountRequests, List<Player> participants) {
        if (discountRequests == null || discountRequests.isEmpty()) {
            return Map.of();
        }

        Set<Long> participantIds = participants.stream().map(Player::getId).collect(Collectors.toSet());
        Map<Long, Double> discounts = new LinkedHashMap<>();

        for (MatchExpenseDiscountRequest request : discountRequests) {
            if (!participantIds.contains(request.getPlayerId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Discounts can only be applied to expense participants"
                );
            }

            discounts.put(request.getPlayerId(), roundToTwoDecimals(request.getAmount()));
        }

        return discounts;
    }

    private MatchExpenseResponse mapToResponse(MatchExpense expense) {
        Map<Long, Double> discountByPlayer = expense.getDiscounts().stream()
                .collect(Collectors.toMap(
                        discount -> discount.getPlayer().getId(),
                        MatchExpenseDiscount::getAmount
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
                                        .map(team -> team.getTeamName())
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

    private String normalizeCategory(String category) {
        return category.trim().toUpperCase();
    }

    private Player findPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private MatchStatus getStatus(Match match) {
        return match.getStatus() == null ? MatchStatus.SCHEDULED : match.getStatus();
    }
}
