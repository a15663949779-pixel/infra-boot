package com.example.system.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long expiresIn;
}
