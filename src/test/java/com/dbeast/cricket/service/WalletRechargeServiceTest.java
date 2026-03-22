package com.dbeast.cricket.service;

import com.dbeast.cricket.dto.RechargeWalletRequest;
import com.dbeast.cricket.dto.WalletRechargeRequestResponse;
import com.dbeast.cricket.entity.Player;
import com.dbeast.cricket.entity.PlayerTeam;
import com.dbeast.cricket.entity.Team;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.entity.Wallet;
import com.dbeast.cricket.entity.WalletRechargeRequestEntity;
import com.dbeast.cricket.entity.WalletRechargeStatus;
import com.dbeast.cricket.entity.WalletRechargeType;
import com.dbeast.cricket.repository.PlayerRepository;
import com.dbeast.cricket.repository.PlayerTeamRepository;
import com.dbeast.cricket.repository.WalletRechargeRequestRepository;
import com.dbeast.cricket.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletRechargeServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @Mock
    private WalletRechargeRequestRepository walletRechargeRequestRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletRechargeService walletRechargeService;

    private Player captain;
    private Player member;
    private Team falcons;

    @BeforeEach
    void setUp() {
        falcons = new Team();
        falcons.setId(11L);
        falcons.setTeamName("Falcons");

        captain = createPlayer(1L, "9990001111", "Captain", 0.0);
        member = createPlayer(2L, "9990002222", "Member", 50.0);
    }

    @Test
    void createRechargeRequestAllowsCaptainDemandForTeamMember() {
        RechargeWalletRequest request = new RechargeWalletRequest();
        request.setPlayerId(member.getId());
        request.setAmount(300.0);
        request.setDescription("Match fee top-up");

        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(playerTeamRepository.findByPlayer(captain))
                .thenReturn(List.of(createMembership(captain, falcons, TeamMemberRole.CAPTAIN)));
        when(playerTeamRepository.findByPlayer(member))
                .thenReturn(List.of(createMembership(member, falcons, TeamMemberRole.MEMBER)));
        when(walletRechargeRequestRepository.save(any(WalletRechargeRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletRechargeRequestResponse response = walletRechargeService.createRechargeRequest(captain.getMobile(), request);

        assertEquals(WalletRechargeType.CAPTAIN_DEMAND, response.getRequestType());
        assertEquals(member.getId(), response.getPlayerId());
        assertEquals("Match fee top-up", response.getDescription());
    }

    @Test
    void approveRechargeRequestCreditsWalletAfterCaptainApproval() {
        WalletRechargeRequestEntity requestEntity = new WalletRechargeRequestEntity();
        requestEntity.setId(91L);
        requestEntity.setPlayer(member);
        requestEntity.setRequestedBy(member);
        requestEntity.setAmount(200.0);
        requestEntity.setStatus(WalletRechargeStatus.PENDING);
        requestEntity.setRequestType(WalletRechargeType.SELF_REQUEST);
        requestEntity.setRequestDate(java.time.LocalDate.now());

        when(walletRechargeRequestRepository.findById(requestEntity.getId())).thenReturn(Optional.of(requestEntity));
        when(playerRepository.findByMobile(captain.getMobile())).thenReturn(Optional.of(captain));
        when(playerTeamRepository.findByPlayer(captain))
                .thenReturn(List.of(createMembership(captain, falcons, TeamMemberRole.CAPTAIN)));
        when(playerTeamRepository.findByPlayer(member))
                .thenReturn(List.of(createMembership(member, falcons, TeamMemberRole.MEMBER)));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRechargeRequestRepository.save(any(WalletRechargeRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletRechargeRequestResponse response = walletRechargeService.approveRechargeRequest(
                requestEntity.getId(),
                captain.getMobile()
        );

        assertEquals(WalletRechargeStatus.APPROVED, response.getStatus());
        assertEquals(250.0, member.getWallet().getBalance());
    }

    private Player createPlayer(Long id, String mobile, String name, double walletBalance) {
        Player player = new Player();
        player.setId(id);
        player.setMobile(mobile);
        player.setName(name);

        Wallet wallet = new Wallet();
        wallet.setId(id + 100);
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
}
