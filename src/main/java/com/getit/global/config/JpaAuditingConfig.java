package com.getit.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @EnableJpaAuditing 을 메인 클래스가 아닌 별도 설정으로 분리한다.
 * 메인 클래스에 붙이면 @WebMvcTest 등 슬라이스 테스트에서 JPA 메타모델을 요구해 실패한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig { }
