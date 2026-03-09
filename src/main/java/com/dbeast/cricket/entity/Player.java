package com.dbeast.cricket.entity; 

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String mobile;

    private String name;

    @Column(unique = true)
    private String mobile;

    private String password;

    private boolean verified; // for OTP login flow

    @ManyToMany(mappedBy = "availablePlayers")
    private Set<Match> matchesAvailable = new HashSet<>();

    // Constructors
    public Player() {}

    public Player(String name, String mobile, String password) {
        this.name = name;
        this.mobile = mobile;
        this.password = password;
        this.verified = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public Set<Match> getMatchesAvailable() { return matchesAvailable; }
    public void setMatchesAvailable(Set<Match> matchesAvailable) { this.matchesAvailable = matchesAvailable; }
}