package com.dbeast.cricket.dto;

import java.time.LocalDate;

public class MatchContributionResponse {

    private final Long id;
    private final Long playerId;
    private final String playerName;
    private final Double amount;
    private final LocalDate contributionDate;
    private final String recordedByName;

    public MatchContributionResponse(
            Long id,
            Long playerId,
            String playerName,
            Double amount,
            LocalDate contributionDate,
            String recordedByName
    ) {
        this.id = id;
        this.playerId = playerId;
        this.playerName = playerName;
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.recordedByName = recordedByName;
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

    public LocalDate getContributionDate() {
        return contributionDate;
    }

    public String getRecordedByName() {
        return recordedByName;
    }
}
