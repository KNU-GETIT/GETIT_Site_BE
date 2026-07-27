package com.getit.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI: /swagger-ui.html · OpenAPI 문서: /v3/api-docs
 * 본 문서는 DOCS/API_명세서.pdf 와 동기화 상태를 유지한다.
 */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme securityScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .in(SecurityScheme.In.HEADER)
        .name("Authorization");

    return new OpenAPI()
        .info(new Info()
            .title("GETIT API")
            .description("GETIT 동아리 통합 사이트 백엔드 API")
            .version("v1"))
        .components(new Components().addSecuritySchemes(BEARER_SCHEME, securityScheme))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }
}
