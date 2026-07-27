package com.getit.domain.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 인증 흐름 설정.
 *
 * @param oauth2RedirectUri 로그인 성공 후 사용자를 돌려보낼 프론트 주소
 * @param refreshCookieSecure Refresh Token 쿠키의 Secure 플래그.
 *                            로컬은 http 라 false, 배포 환경은 반드시 true 여야 한다
 */
@ConfigurationProperties(prefix = "getit.auth")
public record AuthProperties(
    String oauth2RedirectUri,
    boolean refreshCookieSecure
) { }
