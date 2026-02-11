package com.tpms.security;

import com.tpms.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${tpms.jwt.secret}")
    private String secretKeyString;

    @Value("${tpms.jwt.exp-ms}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Print once for debugging
        System.out.println("Initializing JwtUtils...");
        System.out.println("Expiration: " + expirationMs);
        System.out.println("Secret Key: " + secretKeyString);

        // Create signing key
        key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // Generate JWT
    public String generateToken(User user) {

        Date issuedAt = new Date();
        Date exp = new Date(issuedAt.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))   // user ID as subject
                .issuedAt(issuedAt)
                .expiration(exp)
                .claims(Map.of(
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                ))
                .signWith(key)
                .compact();
    }

    // Validate + parse claims
    public Claims validateToken(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    // Extract user id
    public Long getUserId(String token) {
        Claims claims = validateToken(token);
        return Long.valueOf(claims.getSubject());
    }
}
