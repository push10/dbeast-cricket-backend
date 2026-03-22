package com.dbeast.cricket.dto;

public class MatchExpenseDiscountResponse {

    private final Long playerId;
    private final String playerName;
    private final Double amount;
    private final String description;
    private final Long matchId;
    private final String matchLabel;
    private final java.time.LocalDate discountDate;

    public MatchExpenseDiscountResponse(
            Long playerId,
            String playerName,
            Double amount,
            String description,
            Long matchId,
            String matchLabel,
            java.time.LocalDate discountDate
    ) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.amount = amount;
        this.description = description;
        this.matchId = matchId;
        this.matchLabel = matchLabel;
        this.discountDate = discountDate;
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

    public Long getMatchId() {
        return matchId;
    }

    public String getMatchLabel() {
        return matchLabel;
    }

    public java.time.LocalDate getDiscountDate() {
        return discountDate;
    }
}
