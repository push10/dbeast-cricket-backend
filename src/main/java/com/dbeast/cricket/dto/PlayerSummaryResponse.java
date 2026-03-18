package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.PlayerRole;
import com.dbeast.cricket.entity.TeamMemberRole;
import com.dbeast.cricket.entity.UserRole;

public class PlayerSummaryResponse {

    private final Long id;
    private final String name;
    private final String mobile;
    private final String email;
    private final UserRole userRole;
    private final PlayerRole playerRole;
    private final TeamMemberRole teamRole;

    public PlayerSummaryResponse(
            Long id,
            String name,
            String mobile,
            String email,
            UserRole userRole,
            PlayerRole playerRole,
            TeamMemberRole teamRole
    ) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.userRole = userRole;
        this.playerRole = playerRole;
        this.teamRole = teamRole;
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

    public String getEmail() {
        return email;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    public TeamMemberRole getTeamRole() {
        return teamRole;
    }
}
