package com.getit.domain.auth.jwt;

import com.getit.domain.auth.exception.AuthErrorCode;
import com.getit.domain.auth.security.CustomUserDetails;
import com.getit.domain.auth.security.SecurityResponseWriter;
import com.getit.global.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization 헤더의 Bearer 토큰을 검증해 SecurityContext 를 채운다.
 *
 * <p>토큰이 아예 없으면 통과시킨다. 인증이 필요한 경로인지 판단하는 것은 인가 단계의 몫이고,
 * 그때 {@code JwtAuthenticationEntryPoint} 가 401 을 만든다.
 * 반면 토큰이 <em>있는데</em> 만료 · 위조라면 여기서 바로 401 을 내려준다.
 * 그래야 프론트가 TOKEN_EXPIRED 를 보고 재발급을 시도할 수 있다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;
  private final SecurityResponseWriter responseWriter;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    String token = resolveToken(request);

    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Claims claims = jwtProvider.parse(token);
      if (!jwtProvider.isAccessToken(claims)) {
        throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
      }
      SecurityContextHolder.getContext().setAuthentication(toAuthentication(claims, request));
    } catch (BusinessException e) {
      SecurityContextHolder.clearContext();
      responseWriter.write(response, e.getErrorCode());
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  private UsernamePasswordAuthenticationToken toAuthentication(Claims claims, HttpServletRequest request) {
    CustomUserDetails principal = new CustomUserDetails(
        jwtProvider.getUserId(claims),
        jwtProvider.getEmail(claims),
        jwtProvider.getRole(claims)
    );
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    return authentication;
  }
}
