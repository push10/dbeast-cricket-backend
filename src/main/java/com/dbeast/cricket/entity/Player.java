package com.dbeast.cricket.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String mobile;

    private String name;

    private String otp;

    @Column(unique = true)
    private String email;

    private LocalDate dateOfBirth;

    private String address;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column
    private UserRole userRole = UserRole.PLAYER;

    @Enumerated(EnumType.STRING)
    private PlayerRole playerRole;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerTeam> teams = new ArrayList<>();

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Wallet wallet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(PlayerRole playerRole) {
        this.playerRole = playerRole;
    }

    public List<PlayerTeam> getTeams() {
        return teams;
    }

    public void setTeams(List<PlayerTeam> teams) {
        this.teams = teams;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
        if (wallet != null && wallet.getPlayer() != this) {
            wallet.setPlayer(this);
        }
    }
}
