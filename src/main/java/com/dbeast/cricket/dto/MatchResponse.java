package com.dbeast.cricket.dto;

import java.time.LocalDate;

public class MatchResponse {

    private Long id;
    private String teamA;
    private String teamB;
    private LocalDate matchDate;

    // constructor
    public MatchResponse(Long id, String teamA, String teamB, LocalDate matchDate) {
        this.id = id;
        this.teamA = teamA;
        this.teamB = teamB;
        this.matchDate = matchDate;
    }

    // getters
}