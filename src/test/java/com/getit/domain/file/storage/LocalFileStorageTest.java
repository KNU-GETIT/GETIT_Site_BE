package com.getit.domain.file.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalFileStorageTest {

  @TempDir
  private Path tempDir;

  private LocalFileStorage storage() {
    return new LocalFileStorage(tempDir.toString(), "http://localhost:8080/api/public/files");
  }

  @Test
  @DisplayName("업로드: 디스크 저장 + URL 반환")
  void uploadsToDisk() throws IOException {
    LocalFileStorage storage = storage();
    MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "hello".getBytes());

    String url = storage.upload(file, "a.txt");

    assertThat(url).isEqualTo("http://localhost:8080/api/public/files/a.txt");
    assertThat(Files.readString(tempDir.resolve("a.txt"))).isEqualTo("hello");
  }

  @Test
  @DisplayName("삭제: 디스크에서 제거")
  void deletesFromDisk() throws IOException {
    LocalFileStorage storage = storage();
    MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "hello".getBytes());
    storage.upload(file, "a.txt");

    storage.delete("a.txt");

    assertThat(Files.exists(tempDir.resolve("a.txt"))).isFalse();
  }

  @Test
  @DisplayName("존재하지 않는 파일 삭제: 예외 없음")
  void deletingMissingFileIsNoop() {
    LocalFileStorage storage = storage();

    storage.delete("nope.txt");
  }

  @Test
  @DisplayName("경로 조작(key): 거부")
  void rejectsPathTraversal() {
    LocalFileStorage storage = storage();
    MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "hello".getBytes());

    assertThatThrownBy(() -> storage.upload(file, "../../etc/passwd"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
