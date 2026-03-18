package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.TeamMemberRole;

public class TeamMembershipResponse {

    private final Long teamId;
    private final String teamName;
    private final TeamMemberRole role;

    public TeamMembershipResponse(Long teamId, String teamName, TeamMemberRole role) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.role = role;
    }

    public Long getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public TeamMemberRole getRole() {
        return role;
    }
}
