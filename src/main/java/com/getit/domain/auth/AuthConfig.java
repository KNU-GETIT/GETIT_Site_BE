package com.getit.domain.auth;

import com.getit.domain.auth.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, AuthProperties.class})
public class AuthConfig {

  /**
   * Google userinfo 엔드포인트를 호출하는 기본 구현.
   * {@code CustomOAuth2UserService} 가 이 빈에 위임하고 그 결과로 User 를 만든다.
   */
  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService() {
    return new DefaultOAuth2UserService();
  }
}
