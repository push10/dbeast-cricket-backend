package com.dbeast.cricket.service;

import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchContribution;
import com.dbeast.cricket.entity.MatchExpense;
import com.dbeast.cricket.entity.MatchExpenseDiscount;
import com.dbeast.cricket.entity.MatchWalletImpact;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.Wallet;
import com.dbeast.cricket.repository.MatchContributionRepository;
import com.dbeast.cricket.repository.MatchExpenseRepository;
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.MatchWalletImpactRepository;
import com.dbeast.cricket.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchWalletSettlementService {

    private final MatchRepository matchRepository;
    private final MatchExpenseRepository matchExpenseRepository;
    private final MatchContributionRepository matchContributionRepository;
    private final MatchWalletImpactRepository matchWalletImpactRepository;
    private final WalletRepository walletRepository;

    public MatchWalletSettlementService(
            MatchRepository matchRepository,
            MatchExpenseRepository matchExpenseRepository,
            MatchContributionRepository matchContributionRepository,
            MatchWalletImpactRepository matchWalletImpactRepository,
            WalletRepository walletRepository
    ) {
        this.matchRepository = matchRepository;
        this.matchExpenseRepository = matchExpenseRepository;
        this.matchContributionRepository = matchContributionRepository;
        this.matchWalletImpactRepository = matchWalletImpactRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void synchronizeMatchWallets(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        List<MatchExpense> expenses = matchExpenseRepository.findByMatchIdOrderByExpenseDateAscIdAsc(matchId);
        List<MatchContribution> contributions = matchContributionRepository.findByMatchIdOrderByContributionDateAscIdAsc(matchId);

        Map<Long, Player> playersById = new LinkedHashMap<>();
        Map<Long, Double> targetImpactByPlayer = new LinkedHashMap<>();

        for (MatchExpense expense : expenses) {
            Map<Long, Double> discountByPlayer = expense.getDiscounts().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            discount -> discount.getPlayer().getId(),
                            MatchExpenseDiscount::getAmount
                    ));

            expense.getParticipants().forEach(participant -> {
                Player player = participant.getPlayer();
                playersById.putIfAbsent(player.getId(), player);

                double discount = discountByPlayer.getOrDefault(player.getId(), 0.0);
                double netPayable = Math.max(0.0, roundToTwoDecimals(expense.getPerPlayerAmount() - discount));
                targetImpactByPlayer.merge(player.getId(), -netPayable, Double::sum);
            });
        }

        for (MatchContribution contribution : contributions) {
            Player player = contribution.getPlayer();
            playersById.putIfAbsent(player.getId(), player);
            targetImpactByPlayer.merge(player.getId(), -roundToTwoDecimals(contribution.getAmount()), Double::sum);
        }

        Map<Long, MatchWalletImpact> existingImpacts = matchWalletImpactRepository.findByMatchId(matchId).stream()
                .collect(java.util.stream.Collectors.toMap(
                        impact -> impact.getPlayer().getId(),
                        impact -> impact,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        for (Map.Entry<Long, Player> entry : playersById.entrySet()) {
            Long playerId = entry.getKey();
            Player player = entry.getValue();
            double targetAmount = roundToTwoDecimals(targetImpactByPlayer.getOrDefault(playerId, 0.0));

            MatchWalletImpact impact = existingImpacts.get(playerId);
            double previousApplied = impact == null || impact.getAppliedAmount() == null ? 0.0 : impact.getAppliedAmount();
            double delta = roundToTwoDecimals(targetAmount - previousApplied);

            if (Math.abs(delta) > 0.0001) {
                updateWalletBalance(player, delta);
            }

            if (impact == null) {
                impact = new MatchWalletImpact();
                impact.setMatch(match);
                impact.setPlayer(player);
            }

            impact.setAppliedAmount(targetAmount);
            matchWalletImpactRepository.save(impact);
        }
    }

    private void updateWalletBalance(Player player, double delta) {
        Wallet wallet = player.getWallet();

        if (wallet == null) {
            wallet = new Wallet();
            wallet.setBalance(0.0);
            wallet.setPlayer(player);
        }

        wallet.setBalance(roundToTwoDecimals(wallet.getBalance() + delta));
        walletRepository.save(wallet);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
