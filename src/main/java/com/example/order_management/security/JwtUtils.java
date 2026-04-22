package com.example.order_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key key() {
        // Gunakan metode yang sama dengan project user (Base64 atau GetBytes)
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) key())
            .build()
                .parseSignedClaims(token);
        return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Invalid JWT: " + e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .verifyWith((SecretKey) key())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}