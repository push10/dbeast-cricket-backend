package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.PlayerRole;
import com.dbeast.cricket.entity.TeamMemberRole;

public class MatchFinancePlayerResponse {

    private final Long playerId;
    private final String playerName;
    private final String mobile;
    private final String teamName;
    private final PlayerRole playerRole;
    private final TeamMemberRole teamRole;
    private final Double walletBalance;
    private final Double payableAmount;
    private final Double discountAmount;
    private final Double contributionAmount;
    private final Double matchBalance;
    private final boolean insufficientWalletBalance;

    public MatchFinancePlayerResponse(
            Long playerId,
            String playerName,
            String mobile,
            String teamName,
            PlayerRole playerRole,
            TeamMemberRole teamRole,
            Double walletBalance,
            Double payableAmount,
            Double discountAmount,
            Double contributionAmount,
            Double matchBalance,
            boolean insufficientWalletBalance
    ) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.mobile = mobile;
        this.teamName = teamName;
        this.playerRole = playerRole;
        this.teamRole = teamRole;
        this.walletBalance = walletBalance;
        this.payableAmount = payableAmount;
        this.discountAmount = discountAmount;
        this.contributionAmount = contributionAmount;
        this.matchBalance = matchBalance;
        this.insufficientWalletBalance = insufficientWalletBalance;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getTeamName() {
        return teamName;
    }

    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    public TeamMemberRole getTeamRole() {
        return teamRole;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public Double getPayableAmount() {
        return payableAmount;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public Double getContributionAmount() {
        return contributionAmount;
    }

    public Double getMatchBalance() {
        return matchBalance;
    }

    public boolean isInsufficientWalletBalance() {
        return insufficientWalletBalance;
    }
}
