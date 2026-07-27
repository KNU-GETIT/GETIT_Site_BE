package com.getit.domain.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * OAuth2 로그인 진입점. (명세서 1.1)
 *
 * <p>Google 로 나가는 리다이렉트까지만 확인한다. 그 뒤는 외부 서비스라 통합 테스트로 검증할 수 없다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OAuth2LoginFlowTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("인증 없이 Google 로그인 진입점에 접근할 수 있다")
  void authorizationEndpointIsPublic() throws Exception {
    mockMvc.perform(get("/oauth2/authorization/google"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("Google 동의 화면으로 리다이렉트하며 요청한 scope 를 싣는다")
  void redirectsToGoogleWithScopes() throws Exception {
    MvcResult result = mockMvc.perform(get("/oauth2/authorization/google")).andReturn();

    String location = result.getResponse().getRedirectedUrl();
    assertThat(location)
        .startsWith("https://accounts.google.com/o/oauth2/v2/auth")
        .contains("scope=profile%20email")
        .contains("response_type=code")
        .contains("state=");
  }

  @Test
  @DisplayName("STATELESS 정책이지만 authorization request 를 보관할 세션은 만들어진다")
  void createsSessionForAuthorizationRequest() throws Exception {
    MvcResult result = mockMvc.perform(get("/oauth2/authorization/google")).andReturn();

    assertThat(result.getRequest().getSession(false))
        .as("세션이 없으면 콜백에서 authorization request 를 못 찾아 로그인이 실패한다")
        .isNotNull();
  }

  @Test
  @DisplayName("state 가 없는 콜백은 로그인 실패로 처리돼 프론트로 돌아간다")
  void redirectsToFrontendOnFailure() throws Exception {
    MvcResult result = mockMvc.perform(get("/login/oauth2/code/google").param("code", "dummy"))
        .andExpect(status().is3xxRedirection())
        .andReturn();

    assertThat(result.getResponse().getRedirectedUrl())
        .startsWith("http://localhost:5173/oauth/callback")
        .contains("error=OAUTH2_LOGIN_FAILED");
  }
}
