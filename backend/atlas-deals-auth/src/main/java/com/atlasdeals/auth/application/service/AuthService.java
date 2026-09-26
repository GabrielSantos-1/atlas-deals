package com.atlasdeals.auth.application.service;

import com.atlasdeals.auth.application.dto.*;
import com.atlasdeals.auth.domain.Role;
import com.atlasdeals.auth.domain.User;
import com.atlasdeals.auth.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklisted_token:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User(
                request.name(),
                request.email().toLowerCase().trim(),
                passwordEncoder.encode(request.password()),
                Set.of(Role.USER)
        );

        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );

        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        log.info("User registered: {}", user.getEmail());
        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );

        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        log.info("User logged in: {}", user.getEmail());
        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        Claims claims;
        try {
            claims = jwtService.validateToken(request.refreshToken());
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (!jwtService.isRefreshToken(claims)) {
            throw new BadCredentialsException("Invalid token type");
        }

        if (isTokenBlacklisted(request.refreshToken())) {
            throw new BadCredentialsException("Token has been revoked");
        }

        String userId = claims.getSubject();
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );

        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());

        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration());
    }

    public Claims validateToken(String token) {
        if (isTokenBlacklisted(token)) {
            throw new BadCredentialsException("Token has been revoked");
        }
        return jwtService.validateToken(token);
    }

    public void logout(String token) {
        try {
            Claims claims = jwtService.validateToken(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + token,
                        "revoked",
                        ttl,
                        TimeUnit.MILLISECONDS
                );
            }
            log.info("Token invalidated for user: {}", claims.getSubject());
        } catch (Exception e) {
            log.warn("Failed to invalidate token: {}", e.getMessage());
        }
    }

    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }
}
