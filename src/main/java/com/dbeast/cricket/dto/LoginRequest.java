package com.dbeast.cricket.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String mobile;
    private String password;
}