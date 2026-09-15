package com.example.framework.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret = "vibe-admin-default-secret-must-be-at-least-256-bits";
    private Long expiration = 7200L;
    private String header = "Authorization";
    private String tokenPrefix = "Bearer ";
}
