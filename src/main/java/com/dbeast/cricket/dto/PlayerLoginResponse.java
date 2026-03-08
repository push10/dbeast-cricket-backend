package com.dbeast.cricket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerLoginResponse {

    private Long id;
    private String name;
    private String mobile;
    private String token;
}