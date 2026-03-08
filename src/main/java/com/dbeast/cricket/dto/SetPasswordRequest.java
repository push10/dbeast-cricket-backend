package com.dbeast.cricket.dto;

import lombok.Data;

@Data
public class SetPasswordRequest {
    private String mobile;
    private String password;
}