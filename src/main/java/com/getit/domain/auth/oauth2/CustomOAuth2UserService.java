package com.getit.domain.auth.oauth2;

import com.getit.domain.user.dto.OAuthRegistrationResult;
import com.getit.domain.user.dto.OAuthUserRegistration;
import com.getit.domain.user.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Google 프로필을 받아 사용자를 등록하거나 갱신한다. (명세서 1.2)
 *
 * <p>scope 에서 openid 를 뺐기 때문에 OIDC 가 아닌 일반 OAuth2 로 동작하고,
 * 따라서 OidcUserService 가 아니라 이 클래스가 호출된다.
 *
 * <p>{@code DefaultOAuth2UserService} 를 상속하지 않고 주입받아 위임한다.
 * 상속하면 Google 호출 부분만 떼어내 테스트할 수 없다.
 *
 * <p>사용자 생성 · 갱신은 user 패키지의 계약에 맡긴다. auth 는 User 엔티티를 직접 다루지 않는다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
  private final UserAccountService userAccountService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    OAuth2User oAuth2User = delegate.loadUser(userRequest);
    GoogleOAuth2UserInfo userInfo = GoogleOAuth2UserInfo.from(oAuth2User.getAttributes());

    OAuthRegistrationResult result = userAccountService.registerOrUpdateOAuthUser(
        new OAuthUserRegistration(
            userInfo.providerId(),
            userInfo.email(),
            userInfo.name(),
            userInfo.profileImageUrl()
        )
    );

    return new CustomOAuth2User(
        result.account().id(),
        result.account().email(),
        result.account().role(),
        result.newUser(),
        oAuth2User.getAttributes()
    );
  }
}
