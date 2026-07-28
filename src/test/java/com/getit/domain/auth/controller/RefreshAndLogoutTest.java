package com.getit.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.getit.domain.auth.jwt.JwtProvider;
import com.getit.domain.auth.oauth2.RefreshTokenCookie;
import com.getit.domain.auth.service.RefreshTokenService;
import com.getit.domain.user.entity.User;
import com.getit.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/** 1.3 POST /api/auth/refresh · 1.4 POST /api/auth/logout */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RefreshAndLogoutTest {

  private static final String REFRESH_PATH = "/api/auth/refresh";
  private static final String LOGOUT_PATH = "/api/auth/logout";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private JwtProvider jwtProvider;

  private User user;
  private String refreshToken;

  @BeforeEach
  void setUp() {
    user = userRepository.save(
        User.createGuest("google-sub-refresh-api", "refresh-api@getit.com", "김부원", null)
    );
    user.promoteToMember(9);
    userRepository.flush();
    refreshToken = refreshTokenService.issue(user.getId());
  }

  private Cookie refreshCookie(String value) {
    return new Cookie(RefreshTokenCookie.NAME, value);
  }

  @Test
  @DisplayName("쿠키의 Refresh Token 으로 Access Token 을 재발급한다")
  void reissuesAccessToken() throws Exception {
    mockMvc.perform(post(REFRESH_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(1800));
  }

  @Test
  @DisplayName("응답 본문에 Refresh Token 을 담지 않는다")
  void doesNotExposeRefreshTokenInBody() throws Exception {
    mockMvc.perform(post(REFRESH_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
  }

  @Test
  @DisplayName("회전된 새 Refresh Token 을 HttpOnly 쿠키로 다시 내려준다")
  void setsRotatedRefreshCookie() throws Exception {
    MvcResult result = mockMvc.perform(post(REFRESH_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isOk())
        .andReturn();

    String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    assertThat(setCookie).contains(RefreshTokenCookie.NAME + "=").contains("HttpOnly");

    String rotated = result.getResponse().getCookie(RefreshTokenCookie.NAME).getValue();
    assertThat(rotated).isNotEqualTo(refreshToken);
    assertThat(jwtProvider.isRefreshToken(jwtProvider.parse(rotated))).isTrue();
  }

  @Test
  @DisplayName("쿠키가 없으면 401 이다")
  void rejectsMissingCookie() throws Exception {
    mockMvc.perform(post(REFRESH_PATH))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
  }

  @Test
  @DisplayName("이미 회전된 토큰으로 재발급하면 401 이다")
  void rejectsReusedToken() throws Exception {
    mockMvc.perform(post(REFRESH_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isOk());

    mockMvc.perform(post(REFRESH_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
  }

  @Test
  @DisplayName("재발급은 인증 없이 호출할 수 있다 — Access Token 이 만료된 상태에서 부르는 API 다")
  void refreshDoesNotRequireAccessToken() throws Exception {
    mockMvc.perform(post(REFRESH_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("로그아웃하면 204 와 함께 쿠키가 만료된다")
  void logoutExpiresCookie() throws Exception {
    MvcResult result = mockMvc.perform(post(LOGOUT_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isNoContent())
        .andReturn();

    assertThat(result.getResponse().getCookie(RefreshTokenCookie.NAME).getMaxAge()).isZero();
  }

  @Test
  @DisplayName("로그아웃한 토큰으로는 재발급할 수 없다")
  void cannotRefreshAfterLogout() throws Exception {
    mockMvc.perform(post(LOGOUT_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isNoContent());

    mockMvc.perform(post(REFRESH_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Access Token 이 만료돼도 로그아웃할 수 있다")
  void logoutDoesNotRequireAccessToken() throws Exception {
    // 인증을 요구하면 만료된 사용자가 로그아웃을 못 해 Refresh 가 최대 2주 살아남는다.
    mockMvc.perform(post(LOGOUT_PATH).cookie(refreshCookie(refreshToken)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("쿠키 없이 로그아웃해도 204 다")
  void logoutWithoutCookieSucceeds() throws Exception {
    mockMvc.perform(post(LOGOUT_PATH)).andExpect(status().isNoContent());
  }
}
