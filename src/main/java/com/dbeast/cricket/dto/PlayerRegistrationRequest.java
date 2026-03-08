package com.dbeast.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlayerRegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String mobile;
}