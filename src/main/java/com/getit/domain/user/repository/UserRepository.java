package com.getit.domain.user.repository;

import com.getit.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  /** OAuth 로그인 시 기존 사용자 조회에 쓴다. */
  Optional<User> findByProviderId(String providerId);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
