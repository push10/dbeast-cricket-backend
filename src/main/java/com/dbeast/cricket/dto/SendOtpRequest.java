package com.dbeast.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank
    private String mobile;
}