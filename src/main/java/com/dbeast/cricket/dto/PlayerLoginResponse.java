package com.dbeast.cricket.dto;

import com.dbeast.cricket.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerLoginResponse {

    private Long id;
    private String name;
    private String mobile;
    private UserRole userRole;
    private String token;
}
