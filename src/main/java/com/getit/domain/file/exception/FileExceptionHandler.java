package com.getit.domain.file.exception;

import com.getit.global.dto.ApiResponse;
import com.getit.global.dto.ErrorResponse;
import com.getit.global.exception.CommonErrorCode;
import java.io.UncheckedIOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice(basePackages = "com.getit.domain.file.controller")
public class FileExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class, UncheckedIOException.class})
  public ResponseEntity<ApiResponse<Void>> handleStorageFailure(RuntimeException e) {
    log.error("file storage failure", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR)));
  }

  @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class})
  public ResponseEntity<ApiResponse<Void>> handleMultipartError(Exception e) {
    log.warn("multipart request error: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ErrorResponse.of(CommonErrorCode.INVALID_REQUEST, "파일이 첨부되지 않았습니다.")));
  }
}
