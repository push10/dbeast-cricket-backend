package com.dbeast.cricket.dto;

import java.util.List;

public class TeamResponse {

    private final Long id;
    private final String teamName;
    private final List<PlayerSummaryResponse> players;

    public TeamResponse(Long id, String teamName, List<PlayerSummaryResponse> players) {
        this.id = id;
        this.teamName = teamName;
        this.players = players;
    }

    public Long getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public List<PlayerSummaryResponse> getPlayers() {
        return players;
    }
}
