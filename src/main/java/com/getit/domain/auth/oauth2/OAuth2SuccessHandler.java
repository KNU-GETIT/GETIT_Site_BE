package com.getit.domain.auth.oauth2;

import com.getit.domain.auth.AuthProperties;
import com.getit.domain.auth.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 로그인 성공 처리. (명세서 1.2)
 *
 * <p>Refresh Token 은 HttpOnly 쿠키로 심고, 프론트로 리다이렉트만 한다.
 * Access Token 을 쿼리스트링에 실으면 브라우저 히스토리 · Referer · 서버 로그에 남는다.
 * 프론트는 리다이렉트를 받은 뒤 {@code POST /api/auth/refresh} 로 Access Token 을 받아간다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;
  private final RefreshTokenCookie refreshTokenCookie;
  private final AuthProperties authProperties;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {
    CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

    String refreshToken = jwtProvider.createRefreshToken(principal.getUserId());
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        refreshTokenCookie.create(refreshToken, jwtProvider.getRefreshTokenValidity()).toString()
    );

    getRedirectStrategy().sendRedirect(request, response, targetUrl(principal));
  }

  /**
   * 최초 로그인이면 프론트가 지원하기 페이지로 유도한다. GUEST 는 아직 부원이 아니다.
   */
  private String targetUrl(CustomOAuth2User principal) {
    return UriComponentsBuilder.fromUriString(authProperties.oauth2RedirectUri())
        .queryParam("isNewUser", principal.isNewUser())
        .build()
        .toUriString();
  }
}
