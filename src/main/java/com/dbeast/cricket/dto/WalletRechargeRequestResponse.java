package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.WalletRechargeStatus;
import com.dbeast.cricket.entity.WalletRechargeType;

import java.time.LocalDate;

public class WalletRechargeRequestResponse {

    private final Long id;
    private final Long playerId;
    private final String playerName;
    private final Double amount;
    private final String description;
    private final WalletRechargeStatus status;
    private final WalletRechargeType requestType;
    private final LocalDate requestDate;
    private final LocalDate approvedDate;
    private final String requestedByName;
    private final String approvedByName;

    public WalletRechargeRequestResponse(
            Long id,
            Long playerId,
            String playerName,
            Double amount,
            String description,
            WalletRechargeStatus status,
            WalletRechargeType requestType,
            LocalDate requestDate,
            LocalDate approvedDate,
            String requestedByName,
            String approvedByName
    ) {
        this.id = id;
        this.playerId = playerId;
        this.playerName = playerName;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.approvedDate = approvedDate;
        this.requestedByName = requestedByName;
        this.approvedByName = approvedByName;
    }

    public Long getId() {
        return id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public WalletRechargeStatus getStatus() {
        return status;
    }

    public WalletRechargeType getRequestType() {
        return requestType;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public String getApprovedByName() {
        return approvedByName;
    }
}
