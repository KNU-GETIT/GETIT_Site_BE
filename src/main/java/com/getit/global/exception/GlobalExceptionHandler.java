package com.getit.global.exception;

import com.getit.global.dto.ApiResponse;
import com.getit.global.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 전역 예외 처리. 도메인별 @RestControllerAdvice 가 잡지 못한 예외의 최종 방어선이다.
 * 모든 응답은 ApiResponse envelope 로 감싼다. (API 명세서 0.2)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("business exception: {} - {}", errorCode.getCode(), e.getMessage());
    return toResponse(errorCode.getStatus(), ErrorResponse.of(errorCode, e.getMessage()));
  }

  /** @Valid @RequestBody 검증 실패 — fieldErrors 를 함께 내려준다. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
        .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
        .toList();
    return toResponse(
        CommonErrorCode.VALIDATION_FAILED.getStatus(),
        ErrorResponse.of(CommonErrorCode.VALIDATION_FAILED, fieldErrors)
    );
  }

  /** @Validated 가 붙은 파라미터(@RequestParam, @PathVariable) 검증 실패. */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
    List<ErrorResponse.FieldError> fieldErrors = e.getConstraintViolations().stream()
        .map(violation -> new ErrorResponse.FieldError(
            violation.getPropertyPath().toString(),
            violation.getMessage()
        ))
        .toList();
    return toResponse(
        CommonErrorCode.VALIDATION_FAILED.getStatus(),
        ErrorResponse.of(CommonErrorCode.VALIDATION_FAILED, fieldErrors)
    );
  }

  @ExceptionHandler({
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
    log.warn("bad request: {}", e.getMessage());
    return toResponse(
        CommonErrorCode.INVALID_REQUEST.getStatus(),
        ErrorResponse.of(CommonErrorCode.INVALID_REQUEST)
    );
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
    return toResponse(
        CommonErrorCode.FILE_SIZE_EXCEEDED.getStatus(),
        ErrorResponse.of(CommonErrorCode.FILE_SIZE_EXCEEDED)
    );
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    return toResponse(
        CommonErrorCode.METHOD_NOT_ALLOWED.getStatus(),
        ErrorResponse.of(CommonErrorCode.METHOD_NOT_ALLOWED)
    );
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException e) {
    return toResponse(
        CommonErrorCode.UNAUTHORIZED.getStatus(),
        ErrorResponse.of(CommonErrorCode.UNAUTHORIZED)
    );
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
    return toResponse(
        CommonErrorCode.FORBIDDEN.getStatus(),
        ErrorResponse.of(CommonErrorCode.FORBIDDEN)
    );
  }

  /** 매핑되지 않은 URL. catch-all 이 잡으면 500 이 되므로 먼저 404 로 처리한다. */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
    return toResponse(
        CommonErrorCode.RESOURCE_NOT_FOUND.getStatus(),
        ErrorResponse.of(CommonErrorCode.RESOURCE_NOT_FOUND)
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
    log.error("unhandled exception", e);
    return toResponse(
        CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
        ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR)
    );
  }

  private ResponseEntity<ApiResponse<Void>> toResponse(HttpStatus status, ErrorResponse error) {
    return ResponseEntity.status(status).body(ApiResponse.error(error));
  }
}
