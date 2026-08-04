package com.getit.domain.file.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

// 추후 Azure Blob File Storage 추가 시 ConditionalOnProperty 어노테이션 사용
@Component
public class LocalFileStorage implements FileStorage {

  private final Path rootDir;
  private final String baseUrl;

  public LocalFileStorage(
      @Value("${getit.file.local.path:./uploads}")
      String rootPath,
      @Value("${getit.file.local.base-url:http://localhost:8080/api/public/files}")
      String baseUrl
  ) {
    this.rootDir = Path.of(rootPath).toAbsolutePath().normalize();
    this.baseUrl = baseUrl;
  }

  @Override
  public String upload(MultipartFile file, String key) {
    Path target = resolve(key);
    try {
      Files.createDirectories(target.getParent());
      file.transferTo(target);
    } catch (IOException e) {
      throw new UncheckedIOException("파일 업로드 실패: key=" + key, e);
    }
    return baseUrl + "/" + key;
  }

  @Override
  public void delete(String key) {
    try {
      Files.deleteIfExists(resolve(key));
    } catch (IOException e) {
      throw new UncheckedIOException("파일 삭제 실패: key=" + key, e);
    }
  }

  private Path resolve(String key) {
    Path target = rootDir.resolve(key).normalize();
    if (!target.startsWith(rootDir)) {
      throw new IllegalArgumentException("잘못된 파일 키입니다: key=" + key);
    }
    return target;
  }
}
