package com.dbeast.cricket.dto;

import java.time.LocalDate;
import java.util.List;

public class NextMatchSquadResponse {

    private final Long matchId;
    private final String teamA;
    private final String teamB;
    private final LocalDate matchDate;
    private final int availableCount;
    private final List<SquadPlayerResponse> squad;

    public NextMatchSquadResponse(
            Long matchId,
            String teamA,
            String teamB,
            LocalDate matchDate,
            int availableCount,
            List<SquadPlayerResponse> squad
    ) {
        this.matchId = matchId;
        this.teamA = teamA;
        this.teamB = teamB;
        this.matchDate = matchDate;
        this.availableCount = availableCount;
        this.squad = squad;
    }

    public Long getMatchId() {
        return matchId;
    }

    public String getTeamA() {
        return teamA;
    }

    public String getTeamB() {
        return teamB;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public List<SquadPlayerResponse> getSquad() {
        return squad;
    }
}
