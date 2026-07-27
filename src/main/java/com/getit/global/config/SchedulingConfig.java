package com.getit.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 활성화. 도메인별 배치가 늘어나므로 특정 도메인이 아니라 global 에 둔다.
 *
 * <p>현재 사용처 — auth 의 만료 Refresh Token 정리.
 * 예정 — file 의 미연결 업로드 파일 정리 (명세서 13.1).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig { }
