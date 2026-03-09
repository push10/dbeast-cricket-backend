package com.dbeast.cricket.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "myverysecretkeymyverysecretkeymyverysecretkey";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String mobile) {

        return Jwts.builder()
                .setSubject(mobile)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    public String extractMobile(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, String mobile) {

    final String extractedMobile = extractMobile(token);

    return extractedMobile.equals(mobile) && !isTokenExpired(token);
}

private boolean isTokenExpired(String token) {

    Date expiration = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();

    return expiration.before(new Date());
}
}