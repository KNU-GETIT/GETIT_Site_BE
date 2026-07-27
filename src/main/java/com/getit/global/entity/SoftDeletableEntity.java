package com.getit.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 이력 보존이 필요한 엔티티(사용자 · 지원서 · 강의 등)가 상속받는다. (설계 명세서 1.4)
 *
 * <p>조회 시 deletedAt IS NULL 조건은 각 Repository 에서 명시적으로 건다.
 * @SQLRestriction 을 쓰면 어드민의 이력 조회가 막히므로 전역 필터는 걸지 않는다.
 */
@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseTimeEntity {

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    if (deletedAt == null) {
      this.deletedAt = LocalDateTime.now();
    }
  }

  public void restore() {
    this.deletedAt = null;
  }
}
