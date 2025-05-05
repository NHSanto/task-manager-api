package com.taskmanager.config.jwt;

import com.taskmanager.entity.User;
import com.taskmanager.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * Class JwtProvider
 *
 * A utility class responsible for generating, validating, and parsing JSON Web Tokens (JWTs).
 * This class provides methods for working with both access and refresh tokens, handling JWT creation and validation,
 * as well as extracting claims from tokens.
 */
@Slf4j
@Component
public class JwtProvider {

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;

    private final JwtParser accessParser;
    private final JwtParser refreshParser;

    private static final long ACCESS_EXPIRATION_MS = 10 * 60 * 1000; // 10 minutes
    private static final long REFRESH_EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    public JwtProvider(
            @Value("${jwt.access.path}") String accessPath,
            @Value("${jwt.refresh.path}") String refreshPath,
            ResourceLoader resourceLoader
    ) throws IOException {

        // Access token secret
        this.accessSecretKey = loadAndValidateKey(resourceLoader.getResource(accessPath), "Access");
        // Refresh token secret
        this.refreshSecretKey = loadAndValidateKey(resourceLoader.getResource(refreshPath), "Refresh");

        // Pre-compile parsers
        this.accessParser = Jwts.parserBuilder().setSigningKey(accessSecretKey).build();
        this.refreshParser = Jwts.parserBuilder().setSigningKey(refreshSecretKey).build();
    }

    private SecretKey loadAndValidateKey(Resource resource, String label) throws IOException {
        if (!resource.exists()) {
            throw new IOException(label + " key file not found at path: " + resource.getDescription());
        }

        String keyString = new String(resource.getInputStream().readAllBytes()).trim();
        byte[] keyBytes = Decoders.BASE64.decode(keyString);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(label + " secret key must be at least 256 bits (32 bytes)");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(@NonNull User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setIssuer("task-manager-api")
                .setAudience("access")
                .claim("type", "access")
                .claim("login", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("role", user.getRole())
                .claim("userId", user.getId())
                .signWith(accessSecretKey)
                .compact();
    }

    public String generateRefreshToken(@NonNull User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setIssuer("task-manager-api")
                .setAudience("refresh")
                .claim("type", "refresh")
                .claim("role", user.getRole())
                .signWith(refreshSecretKey)
                .compact();
    }

    public Claims getAccessClaims(@NonNull String token) {
        return accessParser.parseClaimsJws(token).getBody();
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return refreshParser.parseClaimsJws(token).getBody();
    }

    public boolean validateAccessToken(@NonNull String token) {
        return validateToken(token, accessParser);
    }

    public boolean validateRefreshToken(@NonNull String token) {
        return validateToken(token, refreshParser);
    }

    private boolean validateToken(@NonNull String token, @NonNull JwtParser parser) {
        try {
            parser.parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Expired JWT token: {}", expEx.getMessage(), expEx);
            throw new AuthException("Token expired: " + expEx.getMessage());
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported JWT token: {}", unsEx.getMessage(), unsEx);
            throw new AuthException("Unsupported JWT: " + unsEx.getMessage());
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed JWT token: {}", mjEx.getMessage(), mjEx);
            throw new AuthException("Malformed JWT: " + mjEx.getMessage());
        } catch (SignatureException sEx) {
            log.error("Invalid JWT signature: {}", sEx.getMessage(), sEx);
            throw new AuthException("Invalid signature: " + sEx.getMessage());
        } catch (Exception e) {
            log.error("Unknown JWT exception: {}", e.getMessage(), e);
            throw new AuthException("Auth exception: " + e.getMessage());
        }
    }
}
