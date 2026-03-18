package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.PlayerRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdatePlayerProfileRequest {

    @Size(min = 2, max = 60, message = "Name must be between 2 and 60 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 120, message = "Email must be at most 120 characters")
    private String email;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Pattern(
            regexp = "^(https?://.*)?$",
            message = "Profile image URL must start with http:// or https://"
    )
    @Size(max = 500, message = "Profile image URL must be at most 500 characters")
    private String profileImageUrl;

    private PlayerRole playerRole;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(PlayerRole playerRole) {
        this.playerRole = playerRole;
    }
}
