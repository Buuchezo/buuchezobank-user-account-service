package com.buuchezo.useraccountservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private Long EXPIRATION;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String username, List<String> roles) {
        return generateToken(username, roles, "CUSTOMER");
    }

    public String generateAdminToken(String username) {
        return generateToken(username, List.of("ADMIN"), "ADMIN");
    }

    public String generateToken(
            String username,
            List<String> roles,
            String authenticationType
    ) {
        return Jwts.builder()
                .subject(username)
                .claim("role", roles)
                .claim("authType", authenticationType)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(
                        new Date(System.currentTimeMillis() + EXPIRATION)
                )
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public String extractAuthenticationType(String token) {
        return extractClaims(
                token,
                claims -> claims.get("authType", String.class)
        );
    }

    public List<String> extractRoles(String token) {
        return extractClaims(
                token,
                claims -> claims.get("role", List.class)
        );
    }

    public <T> T extractClaims(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        final Claims claim = extractAllClaims(token);
        return claimsResolver.apply(claim);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);

        return extractedUsername.equals(username)
                && !isTokenExpired(token);
    }
}
