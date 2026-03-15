package com.dbeast.cricket.entity;

import jakarta.persistence.*;

@Entity
public class MatchAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean available;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    public MatchAvailability() {}

    public Long getId() { return id; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
}