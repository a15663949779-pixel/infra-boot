package com.example.framework.security;

import io.jsonwebtoken.Claims;
import com.example.common.security.LoginUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long userId, String username, String passwordHash) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("credentials", credentialVersion(passwordHash))
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.getExpiration())))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean matchesUser(Claims claims, LoginUser user) {
        return user.isEnabled() && user.isAccountNonLocked() && user.isAccountNonExpired()
                && user.isCredentialsNonExpired()
                && user.getUserId().equals(claims.get("userId", Long.class))
                && user.getUsername().equals(claims.getSubject())
                && credentialVersion(user.getPassword()).equals(claims.get("credentials", String.class));
    }

    private String credentialVersion(String passwordHash) {
        // A password hash change invalidates every previously issued session, without a Redis scan.
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(passwordHash.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Long getExpireSeconds() {
        return properties.getExpiration();
    }
}
