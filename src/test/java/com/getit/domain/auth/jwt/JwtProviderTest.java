package com.getit.domain.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.getit.domain.auth.exception.AuthErrorCode;
import com.getit.domain.user.entity.Role;
import com.getit.global.exception.BusinessException;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

  private static final String SECRET = "test-secret-key-for-getit-authentication-only-do-not-use-in-production";
  private static final String OTHER_SECRET = "another-secret-key-that-is-long-enough-for-hmac-sha256-signing!!";

  private static final Long USER_ID = 12L;
  private static final String EMAIL = "member@getit.com";

  private JwtProvider jwtProvider;

  private JwtProvider providerWith(String secret, Duration accessValidity) {
    return new JwtProvider(new JwtProperties(secret, accessValidity, Duration.ofDays(14)));
  }

  @BeforeEach
  void setUp() {
    jwtProvider = providerWith(SECRET, Duration.ofMinutes(30));
  }

  @Nested
  @DisplayName("Access Token")
  class AccessToken {

    @Test
    @DisplayName("userId · email · role 클레임을 담아 발급한다")
    void containsClaims() {
      String token = jwtProvider.createAccessToken(USER_ID, EMAIL, Role.ADMIN);

      Claims claims = jwtProvider.parse(token);

      assertThat(jwtProvider.getUserId(claims)).isEqualTo(USER_ID);
      assertThat(jwtProvider.getEmail(claims)).isEqualTo(EMAIL);
      assertThat(jwtProvider.getRole(claims)).isEqualTo(Role.ADMIN);
      assertThat(jwtProvider.isAccessToken(claims)).isTrue();
      assertThat(jwtProvider.isRefreshToken(claims)).isFalse();
    }

    @Test
    @DisplayName("만료 시간을 초 단위로 알려준다 (명세서 1.2 accessTokenExpiresIn)")
    void exposesExpiresInSeconds() {
      assertThat(jwtProvider.getAccessTokenExpiresIn()).isEqualTo(1800);
    }
  }

  @Nested
  @DisplayName("Refresh Token")
  class RefreshToken {

    @Test
    @DisplayName("userId 만 담고 access 토큰과 구분된다")
    void isDistinguishableFromAccessToken() {
      String token = jwtProvider.createRefreshToken(USER_ID);

      Claims claims = jwtProvider.parse(token);

      assertThat(jwtProvider.getUserId(claims)).isEqualTo(USER_ID);
      assertThat(jwtProvider.isRefreshToken(claims)).isTrue();
      assertThat(jwtProvider.isAccessToken(claims)).isFalse();
    }
  }

  @Nested
  @DisplayName("검증 실패")
  class Validation {

    @Test
    @DisplayName("만료된 토큰은 TOKEN_EXPIRED 를 던진다")
    void rejectsExpiredToken() {
      JwtProvider expiring = providerWith(SECRET, Duration.ofSeconds(-1));
      String token = expiring.createAccessToken(USER_ID, EMAIL, Role.MEMBER);

      assertThatThrownBy(() -> jwtProvider.parse(token))
          .isInstanceOf(BusinessException.class)
          .extracting(e -> ((BusinessException) e).getErrorCode())
          .isEqualTo(AuthErrorCode.TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 INVALID_TOKEN 을 던진다")
    void rejectsForgedSignature() {
      String forged = providerWith(OTHER_SECRET, Duration.ofMinutes(30))
          .createAccessToken(USER_ID, EMAIL, Role.ADMIN);

      assertThatThrownBy(() -> jwtProvider.parse(forged))
          .isInstanceOf(BusinessException.class)
          .extracting(e -> ((BusinessException) e).getErrorCode())
          .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("형식이 깨진 토큰은 INVALID_TOKEN 을 던진다")
    void rejectsMalformedToken() {
      assertThatThrownBy(() -> jwtProvider.parse("이건.토큰이.아니다"))
          .isInstanceOf(BusinessException.class)
          .extracting(e -> ((BusinessException) e).getErrorCode())
          .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("빈 문자열은 INVALID_TOKEN 을 던진다")
    void rejectsEmptyToken() {
      assertThatThrownBy(() -> jwtProvider.parse(""))
          .isInstanceOf(BusinessException.class)
          .extracting(e -> ((BusinessException) e).getErrorCode())
          .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }
  }
}
