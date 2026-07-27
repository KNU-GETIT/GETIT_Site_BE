package com.getit.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * ⚠️ 임시 설정입니다. auth 작업(R 담당)에서 이 파일을 교체합니다.
 *
 * <p>아직 JWT 필터도 OAuth2 로그인도 없으므로 권한 판정을 할 수 없습니다.
 * 그래서 prod 외 프로파일에서는 전 경로를 열어 A/B 가 각자 API 를 바로 테스트할 수 있게 하고,
 * prod 에서만 /api/public/** 을 제외한 전 경로에 인증을 요구합니다.
 * (인증 수단이 없으므로 실질적으로 401 — 인증 미구현 상태의 배포를 막기 위한 안전장치)
 *
 * <p>auth 구현 시 할 일 (설계 명세서 1.1):
 * <pre>
 * - /api/admin/**  → hasRole('ADMIN')
 * - /api/member/** → hasAnyRole('MEMBER','ADMIN')
 * - /api/public/** → permitAll
 * - JwtAuthenticationFilter 를 UsernamePasswordAuthenticationFilter 앞에 등록
 * - @PreAuthorize 메서드 시큐리티로 이중 방어
 * </pre>
 *
 * <p>본 파일과 application.yml 은 R 소유입니다. 경로 규칙 추가가 필요하면 R 에게 요청하세요.
 * (작업 분할 계획 4.1)
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private static final String[] DOCS_WHITELIST = {
      "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
  };

  private final CorsConfigurationSource corsConfigurationSource;
  private final Environment environment;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    boolean isProd = environment.matchesProfiles("prod");

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    if (isProd) {
      log.warn("[SecurityConfig] 인증이 아직 구현되지 않았습니다. prod 에서는 /api/public/** 외 모든 요청이 401 입니다.");
      http.authorizeHttpRequests(auth -> auth
          .requestMatchers("/api/public/**").permitAll()
          .anyRequest().authenticated());
    } else {
      log.warn("[SecurityConfig] ⚠️ 임시 permitAll 상태입니다 (profile={}). auth 구현 시 반드시 교체하세요.",
          String.join(",", currentProfiles()));
      http.authorizeHttpRequests(auth -> auth
          .requestMatchers(DOCS_WHITELIST).permitAll()
          .anyRequest().permitAll());
    }

    return http.build();
  }

  /** active profile 이 비어 있으면 default profile(local) 이 적용된 상태다. */
  private String[] currentProfiles() {
    String[] active = environment.getActiveProfiles();
    return active.length > 0 ? active : environment.getDefaultProfiles();
  }
}
