package com.marketplace.backend.user.auth.jwt;

import com.marketplace.backend.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JWTService {

  public static final Integer ACCESS_TOKEN_EXPIRATION = 900000; // 15 minutes
  public static final Integer REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days

  @Value("${application.name}")
  private String appName;

  @Value("${token.generation.secret}")
  private String secret;

  public String generateAccessToken(String email, UserRole role) {
    return Jwts.builder()
        .issuer(appName)
        .subject(email)
        .claim("role", role.name())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(getGenerationSecretToken())
        .compact();
  }

  public String generateRefreshToken(String email) {
    return Jwts.builder()
        .issuer(appName)
        .subject(email)
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
        .signWith(getGenerationSecretToken())
        .compact();
  }

  public String extractEmailFromToken(String token) {
    Claims claims = extractAllClaims(token);
    return claims.getSubject();
  }

  public boolean validateAccessToken(String token, UserDetails userDetails) {
    String email = extractEmailFromToken(token);
    return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  public boolean validateRefreshToken(String token) {
    try {
      Claims claims = extractAllClaims(token);
      String tokenType = claims.get("type", String.class);
      return "refresh".equals(tokenType) && !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isTokenExpired(String token) {
    Date expirationDate = extractAllClaims(token).getExpiration();
    return expirationDate.before(new Date());
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getGenerationSecretToken())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getGenerationSecretToken() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }
}
