package com.getit.domain.auth.oauth2;

import com.getit.domain.auth.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 로그인 실패 처리.
 *
 * <p>실패 지점도 브라우저 리다이렉트라 JSON 을 반환할 수 없다.
 * 프론트로 돌려보내고 error 파라미터로 알린다. 원인은 서버 로그로만 남긴다
 * (사용자에게 내부 사유를 노출하지 않는다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private static final String ERROR_CODE = "OAUTH2_LOGIN_FAILED";

  private final AuthProperties authProperties;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException {
    log.warn("Google OAuth2 로그인 실패", exception);

    String targetUrl = UriComponentsBuilder.fromUriString(authProperties.oauth2RedirectUri())
        .queryParam("error", ERROR_CODE)
        .build()
        .toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}
