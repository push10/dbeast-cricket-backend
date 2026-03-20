package com.dbeast.cricket.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class MatchRequest {

    private Long teamId;

    private String teamA;

    @jakarta.validation.constraints.NotBlank(message = "Team B name must not be blank")
    private String teamB;

    @NotNull(message = "Match date is required")
    @FutureOrPresent(message = "Match date cannot be in the past")
    private LocalDate matchDate;

    public MatchRequest() {
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamA() {
        return teamA;
    }

    public void setTeamA(String teamA) {
        this.teamA = teamA;
    }

    public String getTeamB() {
        return teamB;
    }

    public void setTeamB(String teamB) {
        this.teamB = teamB;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }
}
