package com.getit.domain.auth.scheduler;

import com.getit.domain.auth.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 만료된 Refresh Token 정리.
 *
 * <p>Redis 였다면 TTL 이 대신했을 일이다. DB 저장을 택했으므로 직접 지운다.
 * 만료 후에도 잠시 남겨두면 재사용 감지가 동작하므로, 유예를 두고 지운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredRefreshTokenCleaner {

  /** 만료 후 이 기간이 지난 기록만 지운다. 그 전까지는 재사용 감지에 쓰인다. */
  private static final int RETENTION_DAYS = 7;

  private final RefreshTokenRepository refreshTokenRepository;

  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  @Transactional
  public void clean() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
    int deleted = refreshTokenRepository.deleteByExpiresAtBefore(threshold);

    if (deleted > 0) {
      log.info("만료된 Refresh Token {}건 삭제", deleted);
    }
  }
}
