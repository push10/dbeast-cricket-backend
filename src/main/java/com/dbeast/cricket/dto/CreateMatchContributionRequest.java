package com.dbeast.cricket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CreateMatchContributionRequest {

    @NotNull(message = "Player id is required")
    private Long playerId;

    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Contribution amount cannot be negative")
    private Double amount;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
