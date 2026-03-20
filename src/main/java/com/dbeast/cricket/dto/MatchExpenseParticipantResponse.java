package com.dbeast.cricket.dto;

public class MatchExpenseParticipantResponse {

    private final Long playerId;
    private final String playerName;
    private final String teamName;
    private final Double netPayable;

    public MatchExpenseParticipantResponse(Long playerId, String playerName, String teamName, Double netPayable) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamName = teamName;
        this.netPayable = netPayable;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTeamName() {
        return teamName;
    }

    public Double getNetPayable() {
        return netPayable;
    }
}
