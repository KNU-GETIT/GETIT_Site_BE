package com.getit.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 모든 API 응답을 감싸는 공통 envelope. (API 명세서 0.2)
 *
 * <pre>
 * 성공: { "success": true,  "data": { ... }, "error": null }
 * 실패: { "success": false, "data": null,    "error": { "code": ..., "message": ... } }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorResponse error
) {

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, null);
  }

  /** 반환할 data 가 없는 성공 응답. (record 컴포넌트 접근자와 겹쳐 success() 이름은 쓸 수 없다) */
  public static ApiResponse<Void> empty() {
    return new ApiResponse<>(true, null, null);
  }

  public static ApiResponse<Void> error(ErrorResponse error) {
    return new ApiResponse<>(false, null, error);
  }
}
