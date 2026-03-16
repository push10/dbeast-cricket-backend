package com.dbeast.cricket.entity;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamName;

    @OneToMany(mappedBy = "team")
    private List<PlayerTeam> players;
}