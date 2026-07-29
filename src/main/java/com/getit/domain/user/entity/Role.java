package com.getit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한. (API 명세서 0.5)
 *
 * <p>label 은 화면 표기용 한글 값이다. 응답 DTO 에서 {@code roleLabel} 로 함께 내려준다
 * (9.1 사용자 목록). 프론트에 매핑 테이블을 두지 않기 위함이다.
 */
@Getter
@RequiredArgsConstructor
public enum Role {

  GUEST("비회원"),
  MEMBER("부원"),
  ADMIN("운영진");

  private final String label;

  /** Spring Security 의 hasRole() 은 ROLE_ 접두사를 요구한다. */
  public String getAuthority() {
    return "ROLE_" + name();
  }
}
