package com.getit.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 상태. (API 명세서 0.5)
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {

  ACTIVE("활동"),
  WITHDRAWN("탈퇴");

  private final String label;
}
