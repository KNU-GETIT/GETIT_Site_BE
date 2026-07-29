package com.getit.domain.auth.oauth2;

import com.getit.domain.user.entity.User;
import com.getit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Google 프로필을 받아 User 를 생성하거나 갱신한다. (명세서 1.2)
 *
 * <p>scope 에서 openid 를 뺐기 때문에 OIDC 가 아닌 일반 OAuth2 로 동작하고,
 * 따라서 OidcUserService 가 아니라 이 클래스가 호출된다.
 *
 * <p>{@code DefaultOAuth2UserService} 를 상속하지 않고 주입받아 위임한다.
 * 상속하면 Google 호출 부분만 떼어내 테스트할 수 없다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    OAuth2User oAuth2User = delegate.loadUser(userRequest);
    GoogleOAuth2UserInfo userInfo = GoogleOAuth2UserInfo.from(oAuth2User.getAttributes());

    return userRepository.findByProviderId(userInfo.providerId())
        .map(existing -> toPrincipal(refresh(existing, userInfo), oAuth2User, false))
        .orElseGet(() -> toPrincipal(create(userInfo), oAuth2User, true));
  }

  /**
   * 최초 로그인. GUEST 로 생성한다. 지원서를 내기 전이라 연락처 · 학과 · 기수는 비어 있다.
   * 합격 후 운영진이 MEMBER 로 승격한다 (9.4).
   */
  private User create(GoogleOAuth2UserInfo userInfo) {
    return userRepository.save(User.createGuest(
        userInfo.providerId(),
        userInfo.email(),
        userInfo.name(),
        userInfo.profileImageUrl()
    ));
  }

  /** 재로그인. Google 쪽에서 바뀐 이름 · 프로필 이미지를 반영한다. */
  private User refresh(User user, GoogleOAuth2UserInfo userInfo) {
    user.updateProfile(userInfo.name(), userInfo.profileImageUrl());
    return user;
  }

  private CustomOAuth2User toPrincipal(User user, OAuth2User oAuth2User, boolean newUser) {
    return new CustomOAuth2User(
        user.getId(),
        user.getEmail(),
        user.getRole(),
        newUser,
        oAuth2User.getAttributes()
    );
  }
}
