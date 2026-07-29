package com.getit.global.config;

import com.getit.domain.auth.jwt.JwtAuthenticationFilter;
import com.getit.domain.auth.security.JwtAccessDeniedHandler;
import com.getit.domain.auth.security.JwtAuthenticationEntryPoint;
import com.getit.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * URL 기반 인가 + JWT 인증. (설계 명세서 1.1 · API 명세서 0.1)
 *
 * <p>URL 규칙과 {@code @PreAuthorize} 메서드 시큐리티로 이중 방어한다.
 * 본인 리소스 접근(과제 제출물 · 지원서 · 파일)은 서비스 레이어에서 소유자를 검증한다.
 *
 * <p>이 파일과 application.yml 은 R 소유다. 경로 규칙 추가가 필요하면 R 에게 요청한다.
 * (작업 분할 계획 4.1)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  /** 인증 없이 열어두는 경로. */
  private static final String[] PUBLIC_ENDPOINTS = {
      "/api/public/**",
      // OAuth2 로그인 리다이렉트와 콜백 (명세서 1.1 · 1.2)
      "/oauth2/**", "/login/oauth2/**",
      // 토큰 재발급은 Access Token 이 만료된 상태에서 호출된다 (명세서 1.3)
      "/api/auth/refresh", "/api/auth/callback"
  };

  private static final String[] DOCS_ENDPOINTS = {
      "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
  };

  private final CorsConfigurationSource corsConfigurationSource;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;
  private final JwtAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .exceptionHandling(handling -> handling
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))

        .authorizeHttpRequests(auth -> auth
            .requestMatchers(DOCS_ENDPOINTS).permitAll()
            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
            // 지원서는 GUEST 도 쓴다. 인증만 확인한다
            .requestMatchers("/api/applications/**").authenticated()
            .requestMatchers("/api/member/**").hasAnyRole(Role.MEMBER.name(), Role.ADMIN.name())
            .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())
            .requestMatchers("/api/files/**").authenticated()
            .anyRequest().authenticated())

        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
