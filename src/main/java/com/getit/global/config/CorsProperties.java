package com.getit.global.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 프론트 오리진 설정. 환경별로 application-{profile}.yml 에서 덮어쓴다.
 */
@ConfigurationProperties(prefix = "getit.cors")
public record CorsProperties(
    List<String> allowedOrigins
) { }
