package com.getit.domain.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import com.getit.domain.auth.jwt.JwtProvider;
import com.getit.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/** 로그인 성공 시 쿠키 · 리다이렉트 동작. (명세서 1.2) */
@SpringBootTest
class OAuth2SuccessHandlerTest {

  @Autowired
  private OAuth2SuccessHandler successHandler;

  @Autowired
  private JwtProvider jwtProvider;

  private MockHttpServletResponse handle(boolean newUser) throws Exception {
    CustomOAuth2User principal =
        new CustomOAuth2User(42L, "oauth@getit.com", Role.GUEST, newUser, Map.of("sub", "x"));
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

    MockHttpServletResponse response = new MockHttpServletResponse();
    successHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);
    return response;
  }

  @Test
  @DisplayName("Refresh Token 을 HttpOnly 쿠키로 심는다")
  void setsHttpOnlyRefreshCookie() throws Exception {
    MockHttpServletResponse response = handle(true);

    String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
    assertThat(setCookie)
        .isNotNull()
        .contains(RefreshTokenCookie.NAME + "=")
        .contains("HttpOnly")
        .contains("SameSite=Lax")
        .contains("Path=/");
  }

  @Test
  @DisplayName("쿠키에 담긴 값은 해당 사용자의 Refresh Token 이다")
  void cookieHoldsRefreshTokenForUser() throws Exception {
    MockHttpServletResponse response = handle(true);

    String token = response.getCookie(RefreshTokenCookie.NAME).getValue();
    Claims claims = jwtProvider.parse(token);

    assertThat(jwtProvider.isRefreshToken(claims)).isTrue();
    assertThat(jwtProvider.getUserId(claims)).isEqualTo(42L);
  }

  @Test
  @DisplayName("Access Token 은 URL 에 싣지 않는다")
  void doesNotLeakAccessTokenInUrl() throws Exception {
    MockHttpServletResponse response = handle(true);

    assertThat(response.getRedirectedUrl())
        .doesNotContain("accessToken")
        .doesNotContain("token=");
  }

  @Test
  @DisplayName("최초 로그인이면 isNewUser=true 로 프론트에 리다이렉트한다")
  void redirectsWithNewUserFlag() throws Exception {
    assertThat(handle(true).getRedirectedUrl())
        .startsWith("http://localhost:5173/oauth/callback")
        .contains("isNewUser=true");
  }

  @Test
  @DisplayName("재로그인이면 isNewUser=false 다")
  void redirectsWithoutNewUserFlag() throws Exception {
    assertThat(handle(false).getRedirectedUrl()).contains("isNewUser=false");
  }
}
