package com.getit.domain.auth.oauth2;

import com.getit.domain.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 인증 직후의 사용자. {@code OAuth2SuccessHandler} 가 이 값으로 토큰을 만든다.
 *
 * @see CustomOAuth2UserService
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

  private final Long userId;
  private final String email;
  private final Role role;

  /** 최초 로그인 여부. 프론트가 지원하기 페이지로 유도할지 판단한다 (명세서 1.2). */
  private final boolean newUser;

  private final Map<String, Object> attributes;

  public CustomOAuth2User(
      Long userId,
      String email,
      Role role,
      boolean newUser,
      Map<String, Object> attributes
  ) {
    this.userId = userId;
    this.email = email;
    this.role = role;
    this.newUser = newUser;
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.getAuthority()));
  }

  @Override
  public String getName() {
    return String.valueOf(userId);
  }
}
