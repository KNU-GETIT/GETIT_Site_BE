package com.getit.domain.auth.dto;

/**
 * Access Token 재발급 응답. (명세서 1.3)
 *
 * <p>명세서와 달리 refreshToken 을 본문에 담지 않는다. HttpOnly 쿠키로만 내려간다.
 * 본문에 실으면 자바스크립트가 읽을 수 있어 HttpOnly 로 둔 이유가 사라진다.
 *
 * @param accessTokenExpiresIn 만료까지 남은 초
 */
public record TokenResponse(
    String accessToken,
    long accessTokenExpiresIn
) { }
