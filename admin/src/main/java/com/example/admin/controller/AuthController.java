package com.example.admin.controller;

import com.example.common.core.CacheConstants;
import com.example.common.core.Result;
import com.example.common.security.LoginUser;
import com.example.framework.security.JwtProperties;
import com.example.framework.security.JwtTokenProvider;
import com.example.system.domain.dto.LoginRequest;
import com.example.system.domain.vo.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = tokenProvider.createToken(loginUser.getUserId(), loginUser.getUsername());
        stringRedisTemplate.opsForValue().set(
                CacheConstants.LOGIN_TOKEN_KEY + token,
                loginUser.getUsername(),
                tokenProvider.getExpireSeconds(),
                TimeUnit.SECONDS);
        return Result.success(LoginResponse.builder()
                .token(token)
                .tokenType(jwtProperties.getTokenPrefix().trim())
                .expiresIn(tokenProvider.getExpireSeconds())
                .build());
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            String token = bearerToken.substring(jwtProperties.getTokenPrefix().length());
            stringRedisTemplate.delete(CacheConstants.LOGIN_TOKEN_KEY + token);
        }
        return Result.success();
    }
}
