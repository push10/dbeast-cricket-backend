package com.dbeast.cricket.dto;

import java.time.LocalDate;

public class MatchResponse {

    private Long id;
    private String teamA;
    private String teamB;
    private LocalDate matchDate;

    // Default constructor is mandatory for Jackson
    public MatchResponse() {}

    // Constructor for convenience
    public MatchResponse(Long id, String teamA, String teamB, LocalDate matchDate) {
        this.id = id;
        this.teamA = teamA;
        this.teamB = teamB;
        this.matchDate = matchDate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeamA() { return teamA; }
    public void setTeamA(String teamA) { this.teamA = teamA; }

    public String getTeamB() { return teamB; }
    public void setTeamB(String teamB) { this.teamB = teamB; }

    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }
}