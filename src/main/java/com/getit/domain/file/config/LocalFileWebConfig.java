package com.getit.domain.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 추후 Azure Blob File Storage 추가 시 local 모드에서만 등록되도록 ConditionalOnProperty 처리
@Configuration
public class LocalFileWebConfig implements WebMvcConfigurer {

  private final String uploadRootPath;

  public LocalFileWebConfig(@Value("${getit.file.local.path:./uploads}") String uploadRootPath) {
    this.uploadRootPath = uploadRootPath;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/files/**")
        .addResourceLocations("file:" + uploadRootPath + "/");
  }
}
