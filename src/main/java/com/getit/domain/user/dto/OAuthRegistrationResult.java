package com.getit.domain.user.dto;

/**
 * OAuth 로그인 처리 결과.
 *
 * @param newUser 이번 로그인으로 계정이 새로 만들어졌는지.
 *                프론트가 지원하기 페이지로 유도할지 판단한다 (명세서 1.2 isNewUser)
 */
public record OAuthRegistrationResult(
    UserAccount account,
    boolean newUser
) { }
