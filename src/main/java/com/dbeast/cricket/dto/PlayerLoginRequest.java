package com.dbeast.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlayerLoginRequest {

    @NotBlank
    private String mobile;

    @NotBlank
    private String otp;
}