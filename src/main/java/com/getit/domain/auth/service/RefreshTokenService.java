package com.getit.domain.auth.service;

import com.getit.domain.auth.entity.RefreshToken;
import com.getit.domain.auth.exception.AuthErrorCode;
import com.getit.domain.auth.jwt.JwtProvider;
import com.getit.domain.auth.repository.RefreshTokenRepository;
import com.getit.domain.user.dto.UserAccount;
import com.getit.domain.user.exception.UserErrorCode;
import com.getit.domain.user.service.UserAccountService;
import com.getit.global.exception.BusinessException;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh Token 발급 · 회전 · 폐기. (명세서 1.3 · 1.4)
 *
 * <p>단순 재발급 대신 Rotation 을 쓰는 이유는 토큰 탈취 대응이다.
 * 회전하면 정상 사용자와 탈취자가 같은 토큰을 두 번 쓰는 순간이 반드시 생기고,
 * 그 순간을 감지해 해당 사용자의 세션을 전부 끊을 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserAccountService userAccountService;
  private final JwtProvider jwtProvider;

  /** 로그인 성공 시 발급한다. */
  @Transactional
  public String issue(Long userId) {
    String token = jwtProvider.createRefreshToken(userId);
    store(token, userId);
    return token;
  }

  /**
   * Access Token 재발급. 기존 Refresh Token 을 폐기하고 새 것을 함께 발급한다.
   *
   * @return 새 Access · Refresh Token 쌍
   */
  @Transactional
  public TokenPair rotate(String refreshToken) {
    Claims claims = parseRefreshToken(refreshToken);
    RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(refreshToken))
        .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

    if (stored.isRevoked()) {
      handleReuse(stored);
    }
    if (stored.isExpired(LocalDateTime.now())) {
      throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    stored.revoke();

    Long userId = jwtProvider.getUserId(claims);
    UserAccount user = userAccountService.findActiveById(userId)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    String newRefreshToken = jwtProvider.createRefreshToken(userId);
    store(newRefreshToken, userId);

    return new TokenPair(
        jwtProvider.createAccessToken(user.id(), user.email(), user.role()),
        newRefreshToken,
        jwtProvider.getAccessTokenExpiresIn()
    );
  }

  /** 로그아웃. 이미 폐기됐거나 없는 토큰이어도 조용히 넘어간다. */
  @Transactional
  public void revoke(String refreshToken) {
    refreshTokenRepository.findByTokenHash(hash(refreshToken))
        .filter(token -> !token.isRevoked())
        .ifPresent(RefreshToken::revoke);
  }

  /**
   * 이미 폐기된 토큰으로 재발급을 시도했다. 정상 흐름에서는 나올 수 없는 요청이다.
   * 탈취로 보고 해당 사용자의 살아 있는 토큰을 전부 끊는다.
   */
  private void handleReuse(RefreshToken stored) {
    int revoked = refreshTokenRepository.revokeAllByUserId(stored.getUserId(), LocalDateTime.now());
    log.warn("Refresh Token 재사용 감지. userId={}, 무효화한 토큰 수={}", stored.getUserId(), revoked);
    throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
  }

  private Claims parseRefreshToken(String refreshToken) {
    Claims claims = jwtProvider.parse(refreshToken);
    if (!jwtProvider.isRefreshToken(claims)) {
      throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
    return claims;
  }

  private void store(String token, Long userId) {
    refreshTokenRepository.save(RefreshToken.issue(
        hash(token),
        userId,
        LocalDateTime.now().plus(jwtProvider.getRefreshTokenValidity())
    ));
  }

  /** 토큰 원문을 저장하지 않는다. DB 유출 시 그대로 쓸 수 있는 토큰이 나오면 안 된다. */
  private String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 을 사용할 수 없습니다.", e);
    }
  }

  public record TokenPair(String accessToken, String refreshToken, long accessTokenExpiresIn) { }
}
