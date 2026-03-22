package com.dbeast.cricket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class RechargeWalletRequest {

    @NotNull(message = "Recharge amount is required")
    @DecimalMin(value = "0.01", message = "Recharge amount must be greater than 0")
    private Double amount;

    private Long playerId;

    private String description;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
