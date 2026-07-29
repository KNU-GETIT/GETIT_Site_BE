package com.getit.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.getit.domain.auth.exception.AuthErrorCode;
import com.getit.domain.auth.jwt.JwtProvider;
import com.getit.domain.auth.repository.RefreshTokenRepository;
import com.getit.domain.auth.service.RefreshTokenService.TokenPair;
import com.getit.domain.user.entity.User;
import com.getit.domain.user.exception.UserErrorCode;
import com.getit.domain.user.repository.UserRepository;
import com.getit.global.exception.BusinessException;
import com.getit.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Refresh Token Rotation 과 재사용 감지. (명세서 1.3 · 1.4) */
@SpringBootTest
@Transactional
class RefreshTokenServiceTest {

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtProvider jwtProvider;

  private User user;

  @BeforeEach
  void setUp() {
    user = userRepository.save(
        User.createGuest("google-sub-refresh", "refresh@getit.com", "김부원", null)
    );
    user.promoteToMember(9);
    userRepository.flush();
  }

  private ErrorCode errorCodeOf(Throwable e) {
    return ((BusinessException) e).getErrorCode();
  }

  @Nested
  @DisplayName("rotate")
  class Rotate {

    @Test
    @DisplayName("새 Access · Refresh Token 을 발급한다")
    void issuesNewTokenPair() {
      String issued = refreshTokenService.issue(user.getId());

      TokenPair tokens = refreshTokenService.rotate(issued);

      assertThat(tokens.accessToken()).isNotBlank();
      assertThat(tokens.refreshToken()).isNotBlank().isNotEqualTo(issued);
      assertThat(tokens.accessTokenExpiresIn()).isEqualTo(1800);
      assertThat(jwtProvider.getUserId(jwtProvider.parse(tokens.accessToken())))
          .isEqualTo(user.getId());
    }

    @Test
    @DisplayName("회전 후 기존 토큰은 거부된다")
    void rejectsRotatedToken() {
      String issued = refreshTokenService.issue(user.getId());
      refreshTokenService.rotate(issued);

      assertThatThrownBy(() -> refreshTokenService.rotate(issued))
          .isInstanceOf(BusinessException.class)
          .extracting(RefreshTokenServiceTest.this::errorCodeOf)
          .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("새로 받은 토큰으로는 다시 회전할 수 있다")
    void allowsChainedRotation() {
      String issued = refreshTokenService.issue(user.getId());

      TokenPair first = refreshTokenService.rotate(issued);
      TokenPair second = refreshTokenService.rotate(first.refreshToken());

      assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
    }

    @Test
    @DisplayName("저장되지 않은 토큰은 거부된다")
    void rejectsUnknownToken() {
      String neverStored = jwtProvider.createRefreshToken(user.getId());

      assertThatThrownBy(() -> refreshTokenService.rotate(neverStored))
          .isInstanceOf(BusinessException.class)
          .extracting(RefreshTokenServiceTest.this::errorCodeOf)
          .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Access Token 으로는 회전할 수 없다")
    void rejectsAccessToken() {
      String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());

      assertThatThrownBy(() -> refreshTokenService.rotate(accessToken))
          .isInstanceOf(BusinessException.class)
          .extracting(RefreshTokenServiceTest.this::errorCodeOf)
          .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("탈퇴한 사용자의 토큰은 404 로 거부된다")
    void rejectsWithdrawnUser() {
      String issued = refreshTokenService.issue(user.getId());
      user.withdraw();
      userRepository.flush();

      assertThatThrownBy(() -> refreshTokenService.rotate(issued))
          .isInstanceOf(BusinessException.class)
          .extracting(RefreshTokenServiceTest.this::errorCodeOf)
          .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("재사용 감지")
  class ReuseDetection {

    @Test
    @DisplayName("이미 쓴 토큰을 다시 쓰면 해당 사용자의 모든 토큰이 무효화된다")
    void revokesAllTokensOnReuse() {
      String first = refreshTokenService.issue(user.getId());
      TokenPair rotated = refreshTokenService.rotate(first);
      assertThat(refreshTokenRepository.countByUserIdAndRevokedAtIsNull(user.getId())).isEqualTo(1);

      // 탈취자가 이미 쓰인 first 로 재발급을 시도한다
      assertThatThrownBy(() -> refreshTokenService.rotate(first))
          .isInstanceOf(BusinessException.class);

      assertThat(refreshTokenRepository.countByUserIdAndRevokedAtIsNull(user.getId()))
          .as("살아 있던 토큰까지 전부 끊겨야 한다")
          .isZero();

      // 정상 사용자가 들고 있던 토큰도 더 이상 쓸 수 없다
      assertThatThrownBy(() -> refreshTokenService.rotate(rotated.refreshToken()))
          .isInstanceOf(BusinessException.class)
          .extracting(RefreshTokenServiceTest.this::errorCodeOf)
          .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("다른 사용자의 토큰은 영향받지 않는다")
    void doesNotAffectOtherUsers() {
      User other = userRepository.save(
          User.createGuest("google-sub-other", "other@getit.com", "이회원", null)
      );
      String otherToken = refreshTokenService.issue(other.getId());

      String first = refreshTokenService.issue(user.getId());
      refreshTokenService.rotate(first);
      assertThatThrownBy(() -> refreshTokenService.rotate(first))
          .isInstanceOf(BusinessException.class);

      assertThat(refreshTokenService.rotate(otherToken)).isNotNull();
    }
  }

  @Nested
  @DisplayName("revoke")
  class Revoke {

    @Test
    @DisplayName("로그아웃하면 해당 토큰을 더 이상 쓸 수 없다")
    void revokedTokenCannotRotate() {
      String issued = refreshTokenService.issue(user.getId());

      refreshTokenService.revoke(issued);

      assertThat(refreshTokenRepository.countByUserIdAndRevokedAtIsNull(user.getId())).isZero();
      assertThatThrownBy(() -> refreshTokenService.rotate(issued))
          .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("저장되지 않은 토큰으로 로그아웃해도 예외를 던지지 않는다")
    void ignoresUnknownToken() {
      refreshTokenService.revoke(jwtProvider.createRefreshToken(user.getId()));
    }
  }

  @Test
  @DisplayName("토큰 원문이 아니라 해시를 저장한다")
  void storesHashNotRawToken() {
    String issued = refreshTokenService.issue(user.getId());

    assertThat(refreshTokenRepository.findAll())
        .extracting("tokenHash")
        .allSatisfy(hash -> assertThat((String) hash)
            .isNotEqualTo(issued)
            .hasSize(64)
            .matches("[0-9a-f]{64}"));
  }
}
