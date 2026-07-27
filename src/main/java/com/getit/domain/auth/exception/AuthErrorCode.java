package com.getit.domain.auth.exception;

import com.getit.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증 도메인 에러 코드. (API 명세서 0.4)
 *
 * <p>인증 실패 자체(토큰 없음)는 {@code CommonErrorCode.UNAUTHORIZED} 를 쓴다.
 * 같은 code 문자열을 두 enum 에 두면 어느 쪽이 나간 응답인지 추적하기 어렵다.
 * 여기에는 토큰의 구체적인 실패 사유만 둔다.
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");

  private final HttpStatus status;
  private final String message;

  @Override
  public String getCode() {
    return name();
  }
}
