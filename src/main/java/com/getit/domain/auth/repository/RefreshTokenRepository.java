package com.getit.domain.auth.repository;

import com.getit.domain.auth.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * 재사용이 감지된 사용자의 살아 있는 토큰을 전부 폐기한다.
   * 건별로 조회해 revoke 하면 토큰 수만큼 UPDATE 가 나가므로 단일 쿼리로 처리한다.
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update RefreshToken t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
  int revokeAllByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

  int deleteByExpiresAtBefore(LocalDateTime threshold);

  long countByUserIdAndRevokedAtIsNull(Long userId);
}
