package com.dbeast.cricket.dto;

import java.time.LocalDate;

public class MatchResponse {

    private Long id;
    private String teamA;
    private String teamB;
    private LocalDate matchDate;
    private String status;

    private int availableCount;
    private Boolean myStatus;

    public MatchResponse() {}

    public MatchResponse(Long id, String teamA, String teamB, LocalDate matchDate,
                         String status, int availableCount, Boolean myStatus) {
        this.id = id;
        this.teamA = teamA;
        this.teamB = teamB;
        this.matchDate = matchDate;
        this.status = status;
        this.availableCount = availableCount;
        this.myStatus = myStatus;
    }

    public Long getId() { return id; }
    public String getTeamA() { return teamA; }
    public String getTeamB() { return teamB; }
    public LocalDate getMatchDate() { return matchDate; }
    public String getStatus() { return status; }
    public int getAvailableCount() { return availableCount; }
    public Boolean getMyStatus() { return myStatus; }

    public void setId(Long id) { this.id = id; }
    public void setTeamA(String teamA) { this.teamA = teamA; }
    public void setTeamB(String teamB) { this.teamB = teamB; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }
    public void setStatus(String status) { this.status = status; }
    public void setAvailableCount(int availableCount) { this.availableCount = availableCount; }
    public void setMyStatus(Boolean myStatus) { this.myStatus = myStatus; }
}
