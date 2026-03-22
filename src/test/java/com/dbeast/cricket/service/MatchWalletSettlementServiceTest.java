package com.dbeast.cricket.service;

import com.dbeast.cricket.entity.Match;
import com.dbeast.cricket.entity.MatchContribution;
import com.dbeast.cricket.entity.MatchExpense;
import com.dbeast.cricket.entity.MatchExpenseDiscount;
import com.dbeast.cricket.entity.MatchExpenseParticipant;
import com.dbeast.cricket.entity.MatchWalletImpact;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.Wallet;
import com.dbeast.cricket.repository.MatchContributionRepository;
import com.dbeast.cricket.repository.MatchExpenseRepository;
import com.dbeast.cricket.repository.MatchRepository;
import com.dbeast.cricket.repository.MatchWalletImpactRepository;
import com.dbeast.cricket.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchWalletSettlementServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchExpenseRepository matchExpenseRepository;

    @Mock
    private MatchContributionRepository matchContributionRepository;

    @Mock
    private MatchWalletImpactRepository matchWalletImpactRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private MatchWalletSettlementService matchWalletSettlementService;

    private Match match;
    private Player player;

    @BeforeEach
    void setUp() {
        match = new Match();
        setMatchId(match, 88L);
        match.setTeamA("Falcons");
        match.setTeamB("Titans");
        match.setMatchDate(LocalDate.now());

        player = new Player();
        player.setId(7L);
        player.setName("Member");
        player.setMobile("9990007777");

        Wallet wallet = new Wallet();
        wallet.setId(9L);
        wallet.setBalance(100.0);
        player.setWallet(wallet);
    }

    @Test
    void synchronizeMatchWalletsDeductsContributionFromWallet() {
        MatchContribution contribution = new MatchContribution();
        contribution.setMatch(match);
        contribution.setPlayer(player);
        contribution.setAmount(30.0);

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(matchExpenseRepository.findByMatchIdOrderByExpenseDateAscIdAsc(match.getId())).thenReturn(List.of());
        when(matchContributionRepository.findByMatchIdOrderByContributionDateAscIdAsc(match.getId()))
                .thenReturn(List.of(contribution));
        when(matchWalletImpactRepository.findByMatchId(match.getId())).thenReturn(List.of());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchWalletSettlementService.synchronizeMatchWallets(match.getId());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(70.0, walletCaptor.getValue().getBalance());
    }

    @Test
    void synchronizeMatchWalletsAppliesDiscountByReducingNetDebit() {
        MatchExpense expense = new MatchExpense();
        expense.setMatch(match);
        expense.setPerPlayerAmount(120.0);
        expense.setExpenseDate(LocalDate.now());

        MatchExpenseParticipant participant = new MatchExpenseParticipant();
        participant.setMatchExpense(expense);
        participant.setPlayer(player);
        expense.setParticipants(List.of(participant));

        MatchExpenseDiscount discount = new MatchExpenseDiscount();
        discount.setMatchExpense(expense);
        discount.setPlayer(player);
        discount.setAmount(20.0);
        discount.setDescription("Paid in cash");
        expense.setDiscounts(List.of(discount));

        MatchWalletImpact existingImpact = new MatchWalletImpact();
        existingImpact.setMatch(match);
        existingImpact.setPlayer(player);
        existingImpact.setAppliedAmount(0.0);

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(matchExpenseRepository.findByMatchIdOrderByExpenseDateAscIdAsc(match.getId())).thenReturn(List.of(expense));
        when(matchContributionRepository.findByMatchIdOrderByContributionDateAscIdAsc(match.getId())).thenReturn(List.of());
        when(matchWalletImpactRepository.findByMatchId(match.getId())).thenReturn(List.of(existingImpact));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchWalletSettlementService.synchronizeMatchWallets(match.getId());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(0.0, walletCaptor.getValue().getBalance());
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
