package com.getit.global.exception;

import lombok.Getter;

/**
 * 모든 비즈니스 예외의 최상위 타입.
 * 도메인에서는 이 클래스를 직접 던지거나, 도메인 전용 하위 예외를 만들어 사용한다.
 */
@Getter
public class BusinessException extends RuntimeException {

  private final transient ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  /** 사용자에게 보여줄 메시지를 상황에 맞게 덮어써야 할 때 사용한다. */
  public BusinessException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
