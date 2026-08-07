package com.getit.domain.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.getit.domain.auth.jwt.JwtProvider;
import com.getit.domain.user.entity.Role;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * SecurityConfig 의 URL 인가 규칙 검증. (설계 명세서 1.1)
 *
 * <p>검증 대상은 <b>인가 통과 여부</b>지 응답 내용이 아니다.
 * 401·403 이면 인가 단계에서 막힌 것이고, 그 외 상태 코드는 전부 통과한 것이다.
 * 매핑이 없으면 404, 메서드가 다르면 405 가 되는데 둘 다 인가는 통과한 상태다.
 *
 * <p>기대값을 404 로 못박지 않는 이유는 A/B 가 컨트롤러를 추가할 때마다
 * 이 테스트가 깨지기 때문이다. 실제로 file 컨트롤러가 생기면서 한 번 깨졌다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationRuleTest {

  private static final String ADMIN_PATH = "/api/admin/dashboard/summary";
  private static final String MEMBER_PATH = "/api/member/lectures";
  private static final String APPLICATION_PATH = "/api/applications/me";
  private static final String FILE_PATH = "/api/files/1";
  private static final String PUBLIC_PATH = "/api/public/home";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtProvider jwtProvider;

  /** 인가를 통과했음을 뜻한다. 401·403 만 아니면 통과다. */
  private ResultMatcher passedAuthorization() {
    return result -> {
      int actual = result.getResponse().getStatus();
      assertThat(actual)
          .as("인가를 통과하면 401·403 이 아니어야 한다 (실제 %d)", actual)
          .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
    };
  }

  @Nested
  @DisplayName("비인증 요청")
  class Anonymous {

    @Test
    @DisplayName("공개 경로는 인증 없이 통과한다")
    void allowsPublicPath() throws Exception {
      mockMvc.perform(get(PUBLIC_PATH)).andExpect(passedAuthorization());
    }

    @Test
    @DisplayName("Swagger 문서는 인증 없이 열린다")
    void allowsSwagger() throws Exception {
      mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("보호 경로는 401 을 반환한다")
    void rejectsProtectedPaths() throws Exception {
      mockMvc.perform(get(ADMIN_PATH)).andExpect(status().isUnauthorized());
      mockMvc.perform(get(MEMBER_PATH)).andExpect(status().isUnauthorized());
      mockMvc.perform(get(APPLICATION_PATH)).andExpect(status().isUnauthorized());
      mockMvc.perform(get(FILE_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401 응답도 ApiResponse envelope 형식이다")
    void returnsEnvelopeOn401() throws Exception {
      mockMvc.perform(get(ADMIN_PATH))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.data").doesNotExist())
          .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
  }

  @Nested
  @DisplayName("GUEST")
  class Guest {

    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("지원서 경로는 접근할 수 있다")
    void canAccessApplications() throws Exception {
      mockMvc.perform(get(APPLICATION_PATH)).andExpect(passedAuthorization());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("부원 · 운영진 경로는 403 이다")
    void cannotAccessMemberAndAdmin() throws Exception {
      mockMvc.perform(get(MEMBER_PATH)).andExpect(status().isForbidden());
      mockMvc.perform(get(ADMIN_PATH)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("403 응답도 ApiResponse envelope 형식이다")
    void returnsEnvelopeOn403() throws Exception {
      mockMvc.perform(get(ADMIN_PATH))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
  }

  @Nested
  @DisplayName("MEMBER")
  class Member {

    @Test
    @WithMockUser(roles = "MEMBER")
    @DisplayName("부원 경로와 파일 경로에 접근할 수 있다")
    void canAccessMemberArea() throws Exception {
      mockMvc.perform(get(MEMBER_PATH)).andExpect(passedAuthorization());
      mockMvc.perform(get(FILE_PATH)).andExpect(passedAuthorization());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    @DisplayName("운영진 경로는 403 이다")
    void cannotAccessAdmin() throws Exception {
      mockMvc.perform(get(ADMIN_PATH)).andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("ADMIN")
  class Admin {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("모든 영역에 접근할 수 있다")
    void canAccessEverything() throws Exception {
      mockMvc.perform(get(ADMIN_PATH)).andExpect(passedAuthorization());
      mockMvc.perform(get(MEMBER_PATH)).andExpect(passedAuthorization());
      mockMvc.perform(get(APPLICATION_PATH)).andExpect(passedAuthorization());
    }
  }

  @Nested
  @DisplayName("JWT 토큰 인증")
  class TokenAuthentication {

    @Test
    @DisplayName("유효한 ADMIN 토큰으로 운영진 경로에 접근한다")
    void acceptsValidToken() throws Exception {
      String token = jwtProvider.createAccessToken(1L, "admin@getit.com", Role.ADMIN);

      mockMvc.perform(get(ADMIN_PATH).header("Authorization", "Bearer " + token))
          .andExpect(passedAuthorization());
    }

    @Test
    @DisplayName("MEMBER 토큰으로 운영진 경로에 접근하면 403 이다")
    void rejectsInsufficientRole() throws Exception {
      String token = jwtProvider.createAccessToken(1L, "member@getit.com", Role.MEMBER);

      mockMvc.perform(get(ADMIN_PATH).header("Authorization", "Bearer " + token))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("위조된 토큰은 INVALID_TOKEN 으로 401 이다")
    void rejectsForgedToken() throws Exception {
      mockMvc.perform(get(ADMIN_PATH).header("Authorization", "Bearer forged.token.value"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("Refresh Token 으로는 API 에 접근할 수 없다")
    void rejectsRefreshTokenAsAccessToken() throws Exception {
      String refreshToken = jwtProvider.createRefreshToken(1L);

      mockMvc.perform(get(ADMIN_PATH).header("Authorization", "Bearer " + refreshToken))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }
  }
}
