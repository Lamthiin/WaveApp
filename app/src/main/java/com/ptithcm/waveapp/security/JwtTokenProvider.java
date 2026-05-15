package com.ptithcm.waveapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtTokenProvider {
    private String secret = "your-very-long-secret-key-at-least-32-chars-long";
    private long accessExp = 86400000;
    private long refreshExp = 604800000;

    private SecretKey key() { return Keys.hmacShaKeyFor(secret.getBytes()); }

    public String generateAccessToken(String userId, String role) {
        return Jwts.builder().subject(userId).claim("role", role).claim("type","access")
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+accessExp))
                .signWith(key()).compact();
    }
    public String generateRefreshToken(String userId) {
        return Jwts.builder().subject(userId).claim("type","refresh")
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+refreshExp))
                .signWith(key()).compact();
    }
    public String getUserId(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
    }
    public boolean validate(String token) {
        try { Jwts.parser().verifyWith(key()).build().parseSignedClaims(token); return true; }
        catch (Exception e) { return false; }
    }
}
