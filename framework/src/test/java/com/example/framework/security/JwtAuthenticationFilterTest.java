package com.example.framework.security;

import com.example.common.core.CacheConstants;
import com.example.common.security.LoginUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtProperties properties = new JwtProperties();
    private final JwtTokenProvider provider = new JwtTokenProvider(properties);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final UserDetailsService users = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(properties, provider, users, redis);
    private LoginUser user;
    private String token;

    @BeforeEach
    void setup() {
        user = LoginUser.builder().userId(42L).username("alice").password("bcrypt-original")
                .status(1).permissions(Set.of("system:user:list")).build();
        token = provider.createToken(42L, "alice", user.getPassword());
        when(redis.hasKey(anyString())).thenReturn(true);
        when(users.loadUserByUsername("alice")).thenReturn(user);
    }

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void validSessionAuthenticates() throws Exception {
        invoke(token);
        assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void disabledAccountCannotReuseToken() throws Exception {
        user.setStatus(0);
        invoke(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(redis).delete(CacheConstants.LOGIN_TOKEN_KEY + token);
    }

    @Test
    void passwordChangeInvalidatesAllPreviousTokensAndAllowsNewLogin() throws Exception {
        String otherSession = provider.createToken(42L, "alice", user.getPassword());
        user.setPassword("bcrypt-new-password");
        invoke(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        invoke(otherSession);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        invoke(provider.createToken(42L, "alice", user.getPassword()));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void usernameReusedByDifferentUserCannotInheritSession() throws Exception {
        user.setUserId(99L);
        invoke(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deletedUserRemainsUnauthenticatedInsteadOfThrowing() throws Exception {
        when(users.loadUserByUsername("alice")).thenThrow(new UsernameNotFoundException("deleted"));
        invoke(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void loggedOutSessionIsRejected() throws Exception {
        when(redis.hasKey(anyString())).thenReturn(false);
        invoke(token);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(users);
    }

    @Test
    void loginsInSameSecondHaveIndependentTokens() {
        assertNotEquals(token, provider.createToken(42L, "alice", user.getPassword()));
    }

    @Test
    void legacyTokensRequireNewLogin() throws Exception {
        String legacy = Jwts.builder().subject("alice").claim("userId", 42L)
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8))).compact();
        invoke(legacy);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private void invoke(String value) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + value);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }
}
