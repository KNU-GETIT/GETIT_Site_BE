package com.getit.domain.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getit.global.dto.ApiResponse;
import com.getit.global.dto.ErrorResponse;
import com.getit.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * 시큐리티 필터 단계의 에러를 ApiResponse envelope 으로 직렬화한다.
 *
 * <p>필터는 DispatcherServlet 앞에서 동작하므로 @RestControllerAdvice 가 잡지 못한다.
 * 그대로 두면 인증 실패만 응답 형식이 달라져 프론트가 분기해야 한다.
 */
@Component
@RequiredArgsConstructor
public class SecurityResponseWriter {

  private final ObjectMapper objectMapper;

  public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(
        response.getWriter(),
        ApiResponse.error(ErrorResponse.of(errorCode))
    );
  }
}
