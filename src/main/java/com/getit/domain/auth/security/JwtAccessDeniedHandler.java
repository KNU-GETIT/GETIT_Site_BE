package com.getit.domain.auth.security;

import com.getit.global.exception.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** 인증은 됐지만 권한이 부족한 요청 → 403. (API 명세서 0.4 FORBIDDEN) */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final SecurityResponseWriter responseWriter;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException
  ) throws IOException {
    responseWriter.write(response, CommonErrorCode.FORBIDDEN);
  }
}
