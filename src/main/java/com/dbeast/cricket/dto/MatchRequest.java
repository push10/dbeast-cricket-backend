package com.dbeast.cricket.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class MatchRequest {

    @NotBlank(message = "Team A name must not be blank")
    private String teamA;

    @NotBlank(message = "Team B name must not be blank")
    private String teamB;

    @NotNull(message = "Match date is required")
    @FutureOrPresent(message = "Match date cannot be in the past")
    private LocalDate matchDate;

    public MatchRequest() {
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