package com.dbeast.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PlayerLoginRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit Indian mobile number")
    private String mobile;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit number")
    private String otp;
}
