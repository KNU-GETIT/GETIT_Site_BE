package com.getit.domain.auth.jwt;

import com.getit.domain.auth.exception.AuthErrorCode;
import com.getit.domain.user.entity.Role;
import com.getit.global.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * JWT 발급 · 파싱 · 검증. (API 명세서 0.1)
 *
 * <p>OAuth2 로그인과 dev 목 로그인이 모두 이 클래스를 통해 토큰을 만든다.
 */
@Component
public class JwtProvider {

  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_TYPE = "type";
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";

  private final SecretKey key;
  private final Duration accessTokenValidity;
  private final Duration refreshTokenValidity;

  public JwtProvider(JwtProperties properties) {
    this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    this.accessTokenValidity = properties.accessTokenValidity();
    this.refreshTokenValidity = properties.refreshTokenValidity();
  }

  public String createAccessToken(Long userId, String email, Role role) {
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim(CLAIM_EMAIL, email)
        .claim(CLAIM_ROLE, role.name())
        .claim(CLAIM_TYPE, TYPE_ACCESS)
        .issuedAt(new Date())
        .expiration(expirationFrom(accessTokenValidity))
        .signWith(key)
        .compact();
  }

  public String createRefreshToken(Long userId) {
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim(CLAIM_TYPE, TYPE_REFRESH)
        .issuedAt(new Date())
        .expiration(expirationFrom(refreshTokenValidity))
        .signWith(key)
        .compact();
  }

  /**
   * 토큰을 검증하고 클레임을 반환한다.
   *
   * @throws BusinessException 만료 시 TOKEN_EXPIRED, 그 외 서명 · 형식 오류는 INVALID_TOKEN
   */
  public Claims parse(String token) {
    try {
      return Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED);
    } catch (JwtException | IllegalArgumentException e) {
      throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
    }
  }

  public Long getUserId(Claims claims) {
    return Long.valueOf(claims.getSubject());
  }

  public String getEmail(Claims claims) {
    return claims.get(CLAIM_EMAIL, String.class);
  }

  public Role getRole(Claims claims) {
    return Role.valueOf(claims.get(CLAIM_ROLE, String.class));
  }

  public boolean isAccessToken(Claims claims) {
    return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
  }

  public boolean isRefreshToken(Claims claims) {
    return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
  }

  /** 응답의 accessTokenExpiresIn 에 쓴다 (명세서 1.2). 단위는 초다. */
  public long getAccessTokenExpiresIn() {
    return accessTokenValidity.toSeconds();
  }

  public Duration getRefreshTokenValidity() {
    return refreshTokenValidity;
  }

  private Date expirationFrom(Duration validity) {
    return new Date(System.currentTimeMillis() + validity.toMillis());
  }
}
