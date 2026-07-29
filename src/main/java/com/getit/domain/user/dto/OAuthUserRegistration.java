package com.getit.domain.user.dto;

/**
 * OAuth 제공자에게서 받은 프로필. auth 가 user 에게 등록 · 갱신을 요청할 때 쓴다.
 *
 * @param providerId 제공자의 고유 식별자. Google 이면 sub 다
 */
public record OAuthUserRegistration(
    String providerId,
    String email,
    String name,
    String profileImageUrl
) { }
