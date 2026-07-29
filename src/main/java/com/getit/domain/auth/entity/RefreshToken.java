package com.getit.domain.auth.entity;

import com.getit.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 발급된 Refresh Token 의 기록. (명세서 1.3)
 *
 * <p>Rotation 을 하려면 "이 토큰이 이미 쓰였는지" 를 서버가 알아야 하므로 상태를 저장한다.
 *
 * <p>토큰 원문 대신 SHA-256 해시를 저장한다. DB 가 유출돼도 그대로 쓸 수 있는 토큰이 나오지 않는다.
 */
@Entity
@Table(
    name = "refresh_token",
    indexes = @Index(name = "idx_refresh_token_user_id", columnList = "userId")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, columnDefinition = "CHAR(64)")
  private String tokenHash;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  /** 폐기 시각. 값이 있으면 이미 사용되었거나 로그아웃된 토큰이다. */
  @Column
  private LocalDateTime revokedAt;

  @Builder(access = AccessLevel.PRIVATE)
  private RefreshToken(String tokenHash, Long userId, LocalDateTime expiresAt) {
    this.tokenHash = tokenHash;
    this.userId = userId;
    this.expiresAt = expiresAt;
  }

  public static RefreshToken issue(String tokenHash, Long userId, LocalDateTime expiresAt) {
    return RefreshToken.builder()
        .tokenHash(tokenHash)
        .userId(userId)
        .expiresAt(expiresAt)
        .build();
  }

  public void revoke() {
    this.revokedAt = LocalDateTime.now();
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public boolean isExpired(LocalDateTime now) {
    return expiresAt.isBefore(now);
  }
}
