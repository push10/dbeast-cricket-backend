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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WalletRechargeService {

    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final WalletRechargeRequestRepository walletRechargeRequestRepository;
    private final WalletRepository walletRepository;

    public WalletRechargeService(
            PlayerRepository playerRepository,
            PlayerTeamRepository playerTeamRepository,
            WalletRechargeRequestRepository walletRechargeRequestRepository,
            WalletRepository walletRepository
    ) {
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
        this.walletRechargeRequestRepository = walletRechargeRequestRepository;
        this.walletRepository = walletRepository;
    }

    public List<WalletRechargeRequestResponse> getVisibleRequests(String actorMobile) {
        Player actor = findPlayerByMobile(actorMobile);
        Set<String> managedTeamNames = getCaptainTeamNames(actor);

        return walletRechargeRequestRepository.findAll().stream()
                .filter(request -> isVisibleToActor(request, actor, managedTeamNames))
                .sorted(Comparator
                        .comparing(WalletRechargeRequestEntity::getRequestDate, Comparator.reverseOrder())
                        .thenComparing(WalletRechargeRequestEntity::getId, Comparator.reverseOrder()))
                .map(this::mapResponse)
                .toList();
    }

    @Transactional
    public WalletRechargeRequestResponse createRechargeRequest(String actorMobile, RechargeWalletRequest request) {
        Player actor = findPlayerByMobile(actorMobile);
        Player targetPlayer = resolveTargetPlayer(actor, request.getPlayerId());

        WalletRechargeRequestEntity rechargeRequest = new WalletRechargeRequestEntity();
        rechargeRequest.setPlayer(targetPlayer);
        rechargeRequest.setRequestedBy(actor);
        rechargeRequest.setAmount(roundToTwoDecimals(request.getAmount()));
        rechargeRequest.setDescription(normalizeOptionalText(request.getDescription()));
        rechargeRequest.setStatus(WalletRechargeStatus.PENDING);
        rechargeRequest.setRequestType(actor.getId().equals(targetPlayer.getId())
                ? WalletRechargeType.SELF_REQUEST
                : WalletRechargeType.CAPTAIN_DEMAND);
        rechargeRequest.setRequestDate(LocalDate.now());

        return mapResponse(walletRechargeRequestRepository.save(rechargeRequest));
    }

    @Transactional
    public WalletRechargeRequestResponse approveRechargeRequest(Long requestId, String captainMobile) {
        WalletRechargeRequestEntity rechargeRequest = walletRechargeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recharge request not found"));

        if (rechargeRequest.getStatus() != WalletRechargeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recharge request is already processed");
        }

        Player captain = findPlayerByMobile(captainMobile);
        validateCaptainCanManagePlayer(captain, rechargeRequest.getPlayer());

        Wallet wallet = rechargeRequest.getPlayer().getWallet();
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setBalance(0.0);
            wallet.setPlayer(rechargeRequest.getPlayer());
        }

        wallet.setBalance(roundToTwoDecimals(wallet.getBalance() + rechargeRequest.getAmount()));
        walletRepository.save(wallet);

        rechargeRequest.setApprovedBy(captain);
        rechargeRequest.setApprovedDate(LocalDate.now());
        rechargeRequest.setStatus(WalletRechargeStatus.APPROVED);

        return mapResponse(walletRechargeRequestRepository.save(rechargeRequest));
    }

    private Player resolveTargetPlayer(Player actor, Long playerId) {
        if (playerId == null || actor.getId().equals(playerId)) {
            return actor;
        }

        Player targetPlayer = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        validateCaptainCanManagePlayer(actor, targetPlayer);
        return targetPlayer;
    }

    private boolean isVisibleToActor(
            WalletRechargeRequestEntity request,
            Player actor,
            Set<String> managedTeamNames
    ) {
        if (request.getPlayer().getId().equals(actor.getId()) || request.getRequestedBy().getId().equals(actor.getId())) {
            return true;
        }

        if (managedTeamNames.isEmpty()) {
            return false;
        }

        return playerTeamRepository.findByPlayer(request.getPlayer()).stream()
                .map(PlayerTeam::getTeam)
                .map(Team::getTeamName)
                .anyMatch(managedTeamNames::contains);
    }

    private void validateCaptainCanManagePlayer(Player captain, Player targetPlayer) {
        Set<String> captainTeamNames = getCaptainTeamNames(captain);
        if (captainTeamNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a captain can manage wallet recharges");
        }

        boolean sharesManagedTeam = playerTeamRepository.findByPlayer(targetPlayer).stream()
                .map(PlayerTeam::getTeam)
                .map(Team::getTeamName)
                .anyMatch(captainTeamNames::contains);

        if (!sharesManagedTeam) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Captain can only manage players from their team");
        }
    }

    private Set<String> getCaptainTeamNames(Player player) {
        return playerTeamRepository.findByPlayer(player).stream()
                .filter(playerTeam -> playerTeam.getRole() == TeamMemberRole.CAPTAIN)
                .map(PlayerTeam::getTeam)
                .map(Team::getTeamName)
                .collect(Collectors.toSet());
    }

    private Player findPlayerByMobile(String mobile) {
        return playerRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private WalletRechargeRequestResponse mapResponse(WalletRechargeRequestEntity request) {
        return new WalletRechargeRequestResponse(
                request.getId(),
                request.getPlayer().getId(),
                request.getPlayer().getName(),
                request.getAmount(),
                request.getDescription(),
                request.getStatus(),
                request.getRequestType(),
                request.getRequestDate(),
                request.getApprovedDate(),
                request.getRequestedBy().getName(),
                request.getApprovedBy() == null ? null : request.getApprovedBy().getName()
        );
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
