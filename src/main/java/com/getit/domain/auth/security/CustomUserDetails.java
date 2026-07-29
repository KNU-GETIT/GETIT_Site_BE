package com.getit.domain.auth.security;

import com.getit.domain.user.entity.Role;
import com.getit.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 인증된 사용자. 컨트롤러에서 {@code @AuthenticationPrincipal} 로 받아 userId 만 서비스에 넘긴다.
 *
 * <p>토큰 클레임만으로 만들 수 있어야 하므로 User 엔티티를 필드로 갖지 않는다.
 * 매 요청마다 DB 를 조회하지 않기 위함이다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

  private final Long userId;
  private final String email;
  private final Role role;

  public CustomUserDetails(Long userId, String email, Role role) {
    this.userId = userId;
    this.email = email;
    this.role = role;
  }

  public static CustomUserDetails from(User user) {
    return new CustomUserDetails(user.getId(), user.getEmail(), user.getRole());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.getAuthority()));
  }

  /** 비밀번호 인증을 쓰지 않는다. Google OAuth2 단일 로그인이다. */
  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
