package com.getit.domain.auth.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정. 시크릿은 커밋하지 않고 .env 의 JWT_SECRET 에서 주입받는다.
 *
 * @param secret HS256 서명 키. 32바이트 이상이어야 한다 (openssl rand -base64 48)
 * @param accessTokenValidity Access Token 만료. 명세서 기준 30분
 * @param refreshTokenValidity Refresh Token 만료. 명세서 기준 2주
 */
@ConfigurationProperties(prefix = "getit.jwt")
public record JwtProperties(
    String secret,
    Duration accessTokenValidity,
    Duration refreshTokenValidity
) { }
