package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.PlayerRole;

public class SquadPlayerResponse {

    private final Long id;
    private final String name;
    private final String mobile;
    private final PlayerRole playerRole;

    public SquadPlayerResponse(Long id, String name, String mobile, PlayerRole playerRole) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.playerRole = playerRole;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public PlayerRole getPlayerRole() {
        return playerRole;
    }
}
