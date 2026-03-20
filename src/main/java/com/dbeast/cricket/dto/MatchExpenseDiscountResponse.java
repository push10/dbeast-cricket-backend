package com.dbeast.cricket.dto;

public class MatchExpenseDiscountResponse {

    private final Long playerId;
    private final String playerName;
    private final Double amount;

    public MatchExpenseDiscountResponse(Long playerId, String playerName, Double amount) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.amount = amount;
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
}
