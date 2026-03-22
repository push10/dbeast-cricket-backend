package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.PlayerRole;
import com.dbeast.cricket.entity.TeamMemberRole;

public class MatchAvailabilityPlayerResponse {

    private final Long id;
    private final String name;
    private final String mobile;
    private final PlayerRole playerRole;
    private final String teamName;
    private final TeamMemberRole teamRole;
    private final boolean available;

    public MatchAvailabilityPlayerResponse(
            Long id,
            String name,
            String mobile,
            PlayerRole playerRole,
            String teamName,
            TeamMemberRole teamRole,
            boolean available
    ) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.playerRole = playerRole;
        this.teamName = teamName;
        this.teamRole = teamRole;
        this.available = available;
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

    public String getTeamName() {
        return teamName;
    }

    public TeamMemberRole getTeamRole() {
        return teamRole;
    }

    public boolean isAvailable() {
        return available;
    }
}
