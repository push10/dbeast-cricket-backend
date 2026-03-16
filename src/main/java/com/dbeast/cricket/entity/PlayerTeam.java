package com.dbeast.cricket.entity;

import jakarta.persistence.*;

@Entity
public class PlayerTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private String role; // CAPTAIN / MEMBER
}