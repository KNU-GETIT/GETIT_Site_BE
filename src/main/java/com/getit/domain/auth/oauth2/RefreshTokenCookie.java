package com.getit.domain.auth.oauth2;

import com.getit.domain.auth.AuthProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Refresh Token 을 담는 쿠키를 만든다.
 *
 * <p>HttpOnly 라 자바스크립트가 읽을 수 없다. localStorage 에 두면 XSS 한 번에 탈취된다.
 * SameSite=Lax 는 OAuth2 리다이렉트(top-level navigation)에서는 전송되고
 * 서드파티 요청에서는 빠지므로 CSRF 를 막으면서 로그인 흐름을 깨지 않는다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookie {

  public static final String NAME = "refresh_token";

  private final AuthProperties authProperties;

  public ResponseCookie create(String token, Duration validity) {
    return base(token)
        .maxAge(validity)
        .build();
  }

  /** 로그아웃 시 즉시 만료시킨다. */
  public ResponseCookie expired() {
    return base("")
        .maxAge(0)
        .build();
  }

  private ResponseCookie.ResponseCookieBuilder base(String value) {
    return ResponseCookie.from(NAME, value)
        .httpOnly(true)
        .secure(authProperties.refreshCookieSecure())
        .sameSite("Lax")
        .path("/");
  }
}
