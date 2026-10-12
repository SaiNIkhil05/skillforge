package com.skillforge.service;

import com.skillforge.exception.TokenRefreshException;
import com.skillforge.model.RefreshToken;
import com.skillforge.model.User;
import com.skillforge.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration:604800000}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String tokenVal = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusMillis(refreshExpirationMs);

        RefreshToken refreshToken = new RefreshToken(user, tokenVal, expiry);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String requestToken) {
        if (requestToken == null || requestToken.isBlank()) {
            throw new TokenRefreshException("Refresh token cannot be blank");
        }

        RefreshToken current = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new TokenRefreshException(requestToken, "Refresh token does not exist in registry"));

        User user = current.getUser();

        // 1. REUSE DETECTION: If token was already revoked, someone may have compromised it
        if (current.isRevoked()) {
            log.warn("SECURITY ALERT: Refresh token reuse detected for user {} with token {}", user.getEmail(), requestToken);
            refreshTokenRepository.revokeAllByUser(user);
            throw new TokenRefreshException(requestToken, "Security alert: Refresh token reuse detected. All active sessions revoked.");
        }

        // 2. EXPIRATION CHECK
        if (current.isExpired()) {
            current.setRevoked(true);
            refreshTokenRepository.save(current);
            throw new TokenRefreshException(requestToken, "Refresh token has expired. Please log in again.");
        }

        // 3. ROTATION: Invalidate current token and issue new one
        current.setRevoked(true);
        refreshTokenRepository.save(current);

        return createRefreshToken(user);
    }

    @Transactional
    public void revokeToken(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.revokeByToken(token);
        }
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        if (user != null) {
            refreshTokenRepository.revokeAllByUser(user);
        }
    }
}
