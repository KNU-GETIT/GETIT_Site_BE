package com.getit.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.getit.global.exception.ErrorCode;
import java.util.List;

/**
 * 실패 응답의 error 필드. (API 명세서 0.2)
 * fieldErrors 는 @Valid 검증 실패(VALIDATION_FAILED) 시에만 채운다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    List<FieldError> fieldErrors
) {

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
  }

  public static ErrorResponse of(ErrorCode errorCode, String message) {
    return new ErrorResponse(errorCode.getCode(), message, null);
  }

  public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
    return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), fieldErrors);
  }

  public record FieldError(String field, String reason) { }
}
