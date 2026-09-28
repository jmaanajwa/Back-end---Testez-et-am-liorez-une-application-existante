package com.openclassrooms.etudiant.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Generates and verifies JWTs used to authenticate users.
 */
@Service
public class JwtService {

    /**
     * The Base64-encoded signing key used to sign and verify tokens.
     */
    private static final String SECRET_KEY = "c29tZXNlY3JldGtleWZvcnRlc3RpbmcxMjM0NTY3ODk=";

    /**
     * Generates an HS256-signed JWT valid for 24 hours, with the login as its subject.
     *
     * @param userDetails the user for whom to create the token
     * @return the signed JWT in compact format
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Checks the JWT signature and validity before extracting its subject.
     *
     * @param token the signed JWT in compact format
     * @return the login stored in the subject, or {@code null} if the subject is absent
     * @throws io.jsonwebtoken.JwtException if the token cannot be verified, including when it has expired
     * @throws IllegalArgumentException if the token is null or empty
     */
    public String extractLogin(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Decodes the Base64 key and creates the HMAC key.
     *
     * @return the secret key used to sign and verify JWTs
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
