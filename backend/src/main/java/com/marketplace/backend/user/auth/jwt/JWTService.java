package com.marketplace.backend.user.auth.jwt;

import com.marketplace.backend.models.User;
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

  public static final Integer ONE_DAY_MILLIS = 86400000;

  @Value("${application.name}")
  private String appName;

  @Value("${token.generation.secret}")
  private String secret;

  public String generateToken(User user) {
    SecretKey key = getGenerationSecretToken();

    return Jwts.builder()
        .issuer(appName)
        .subject(user.getEmail())
        .claim("role", user.getRole().name())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ONE_DAY_MILLIS))
        .signWith(key)
        .compact();
  }

  public String extractEmailFromToken(String token) {
    Claims claims = extractAllClaims(token);
    return claims.getSubject();
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    String email = extractEmailFromToken(token);
    return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
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
