package com.dbeast.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AddPlayerToTeamRequest {

    @NotBlank(message = "Player mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Player mobile number must be a valid 10-digit Indian mobile number")
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
