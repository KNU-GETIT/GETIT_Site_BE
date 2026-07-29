package com.getit.domain.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.getit.domain.user.entity.Role;
import com.getit.domain.user.entity.User;
import com.getit.domain.user.entity.UserStatus;
import com.getit.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Google 프로필 → User 생성 · 갱신. (명세서 1.2)
 *
 * <p>Google 호출은 위임 빈을 목으로 바꿔 대체하고, 그 뒤의 도메인 처리를 검증한다.
 */
@SpringBootTest
@Transactional
class CustomOAuth2UserServiceTest {

  private static final String PROVIDER_ID = "google-sub-oauth-1";
  private static final String EMAIL = "oauth@getit.com";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CustomOAuth2UserService oAuth2UserService;

  /**
   * CustomOAuth2UserService 자신도 같은 인터페이스를 구현하므로 타입만으로는 특정할 수 없다.
   * 위임 대상 빈을 이름으로 지정한다.
   */
  @MockitoBean(name = "defaultOAuth2UserService")
  private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

  private void stubGoogleResponse(String name, String picture) {
    OAuth2User googleUser = new DefaultOAuth2User(
        List.of(),
        Map.of("sub", PROVIDER_ID, "email", EMAIL, "name", name, "picture", picture),
        "sub"
    );
    given(delegate.loadUser(any())).willReturn(googleUser);
  }

  @BeforeEach
  void setUp() {
    stubGoogleResponse("홍길동", "https://lh3.googleusercontent.com/a");
  }

  @Test
  @DisplayName("최초 로그인이면 GUEST 로 User 를 생성하고 isNewUser 가 true 다")
  void createsGuestOnFirstLogin() {
    CustomOAuth2User principal = (CustomOAuth2User) oAuth2UserService.loadUser(null);

    assertThat(principal.isNewUser()).isTrue();
    assertThat(principal.getRole()).isEqualTo(Role.GUEST);
    assertThat(principal.getEmail()).isEqualTo(EMAIL);

    User saved = userRepository.findByProviderId(PROVIDER_ID).orElseThrow();
    assertThat(saved.getName()).isEqualTo("홍길동");
    assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(saved.getGenerationNo()).isNull();
    assertThat(saved.getPhoneNumber()).isNull();
  }

  @Test
  @DisplayName("재로그인이면 isNewUser 가 false 이고 새 User 를 만들지 않는다")
  void doesNotDuplicateOnSecondLogin() {
    oAuth2UserService.loadUser(null);
    long countAfterFirst = userRepository.count();

    CustomOAuth2User principal = (CustomOAuth2User) oAuth2UserService.loadUser(null);

    assertThat(principal.isNewUser()).isFalse();
    assertThat(userRepository.count()).isEqualTo(countAfterFirst);
  }

  @Test
  @DisplayName("재로그인 시 Google 쪽에서 바뀐 이름 · 프로필 이미지를 반영한다")
  void refreshesProfileOnRelogin() {
    oAuth2UserService.loadUser(null);

    stubGoogleResponse("홍길동2", "https://lh3.googleusercontent.com/b");
    oAuth2UserService.loadUser(null);

    User updated = userRepository.findByProviderId(PROVIDER_ID).orElseThrow();
    assertThat(updated.getName()).isEqualTo("홍길동2");
    assertThat(updated.getProfileImageUrl()).isEqualTo("https://lh3.googleusercontent.com/b");
  }

  @Test
  @DisplayName("승격된 사용자가 재로그인해도 권한이 유지된다")
  void keepsRoleOnRelogin() {
    oAuth2UserService.loadUser(null);
    User user = userRepository.findByProviderId(PROVIDER_ID).orElseThrow();
    user.promoteToMember(9);
    userRepository.flush();

    CustomOAuth2User principal = (CustomOAuth2User) oAuth2UserService.loadUser(null);

    assertThat(principal.getRole()).isEqualTo(Role.MEMBER);
    assertThat(principal.isNewUser()).isFalse();
  }
}
