package com.dbeast.cricket.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;       // e.g., "15 Mar 2026"
    private String opponent;   // opponent team name
    private String ground;     // ground name

    private int availableCount; // number of players who confirmed availability

    @ManyToMany
    @JoinTable(
        name = "match_player_availability",
        joinColumns = @JoinColumn(name = "match_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> availablePlayers = new HashSet<>();

    // Constructors
    public Match() {}

    public Match(String date, String opponent, String ground) {
        this.date = date;
        this.opponent = opponent;
        this.ground = ground;
        this.availableCount = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getOpponent() { return opponent; }
    public void setOpponent(String opponent) { this.opponent = opponent; }

    public String getGround() { return ground; }
    public void setGround(String ground) { this.ground = ground; }

    public int getAvailableCount() { return availableCount; }
    public void setAvailableCount(int availableCount) { this.availableCount = availableCount; }

    public Set<Player> getAvailablePlayers() { return availablePlayers; }
    public void setAvailablePlayers(Set<Player> availablePlayers) { this.availablePlayers = availablePlayers; }
}