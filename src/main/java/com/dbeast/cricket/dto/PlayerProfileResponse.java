package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.PlayerRole;
import com.dbeast.cricket.entity.UserRole;

import java.time.LocalDate;
import java.util.List;

public class PlayerProfileResponse {

    private final Long id;
    private final String name;
    private final String mobile;
    private final String email;
    private final LocalDate dateOfBirth;
    private final String address;
    private final String profileImageUrl;
    private final UserRole userRole;
    private final PlayerRole playerRole;
    private final WalletResponse wallet;
    private final List<TeamMembershipResponse> teams;

    public PlayerProfileResponse(
            Long id,
            String name,
            String mobile,
            String email,
            LocalDate dateOfBirth,
            String address,
            String profileImageUrl,
            UserRole userRole,
            PlayerRole playerRole,
            WalletResponse wallet,
            List<TeamMembershipResponse> teams
    ) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.userRole = userRole;
        this.playerRole = playerRole;
        this.wallet = wallet;
        this.teams = teams;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    public WalletResponse getWallet() {
        return wallet;
    }

    public List<TeamMembershipResponse> getTeams() {
        return teams;
    }
}
