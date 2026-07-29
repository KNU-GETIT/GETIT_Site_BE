package com.getit.domain.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.getit.domain.auth.jwt.JwtProvider;
import com.getit.domain.user.entity.User;
import com.getit.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** 1.5 GET /api/auth/me */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

  private static final String ME_PATH = "/api/auth/me";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtProvider jwtProvider;

  @Autowired
  private UserRepository userRepository;

  private User member;

  @BeforeEach
  void setUp() {
    member = userRepository.save(
        User.createGuest("google-sub-me", "member@getit.com", "김부원", "https://cdn.getit.com/1.png")
    );
    member.updateApplicantInfo("010-1234-5678", "경영대학", "경영학과", 3, "2021110000");
    member.promoteToMember(9);
    userRepository.flush();
  }

  private String bearerFor(User user) {
    return "Bearer " + jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
  }

  @Test
  @DisplayName("명세서 1.5 의 필드를 그대로 반환한다")
  void returnsProfileFields() throws Exception {
    mockMvc.perform(get(ME_PATH).header("Authorization", bearerFor(member)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.error").doesNotExist())
        .andExpect(jsonPath("$.data.id").value(member.getId()))
        .andExpect(jsonPath("$.data.email").value("member@getit.com"))
        .andExpect(jsonPath("$.data.name").value("김부원"))
        .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
        .andExpect(jsonPath("$.data.college").value("경영대학"))
        .andExpect(jsonPath("$.data.major").value("경영학과"))
        .andExpect(jsonPath("$.data.studentYear").value(3))
        .andExpect(jsonPath("$.data.studentNumber").value("2021110000"))
        .andExpect(jsonPath("$.data.profileImageUrl").value("https://cdn.getit.com/1.png"))
        .andExpect(jsonPath("$.data.role").value("MEMBER"))
        .andExpect(jsonPath("$.data.generationNo").value(9))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("지원서를 내기 전 GUEST 는 미수집 필드가 null 로 내려간다")
  void returnsNullsForGuest() throws Exception {
    User guest = userRepository.save(
        User.createGuest("google-sub-guest", "guest@getit.com", "홍길동", null)
    );

    mockMvc.perform(get(ME_PATH).header("Authorization", bearerFor(guest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.role").value("GUEST"))
        .andExpect(jsonPath("$.data.phoneNumber").isEmpty())
        .andExpect(jsonPath("$.data.studentNumber").isEmpty())
        .andExpect(jsonPath("$.data.generationNo").isEmpty());
  }

  @Test
  @DisplayName("토큰이 없으면 401 이다")
  void rejectsAnonymous() throws Exception {
    mockMvc.perform(get(ME_PATH))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
  }

  @Test
  @DisplayName("만료 · 위조 토큰은 401 이다")
  void rejectsInvalidToken() throws Exception {
    mockMvc.perform(get(ME_PATH).header("Authorization", "Bearer forged.token.value"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
  }

  @Test
  @DisplayName("탈퇴한 사용자의 토큰은 404 USER_NOT_FOUND 다")
  void rejectsWithdrawnUser() throws Exception {
    String token = bearerFor(member);
    member.withdraw();
    userRepository.flush();

    mockMvc.perform(get(ME_PATH).header("Authorization", token))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
  }

  @Test
  @DisplayName("삭제된 사용자를 가리키는 토큰은 404 USER_NOT_FOUND 다")
  void rejectsUnknownUser() throws Exception {
    String token = "Bearer " + jwtProvider.createAccessToken(
        999_999L, "ghost@getit.com", member.getRole());

    mockMvc.perform(get(ME_PATH).header("Authorization", token))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
  }
}
