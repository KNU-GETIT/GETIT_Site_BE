package com.getit.domain.file.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileAssetTest {

  private FileAsset uploadedFile() {
    return FileAsset.upload("key.txt", "original.txt", "http://localhost/x.txt", 10L, "text/plain", 1L);
  }

  @Test
  @DisplayName("업로드 직후: PENDING")
  void startsAsPending() {
    FileAsset file = uploadedFile();

    assertThat(file.getStatus()).isEqualTo(FileStatus.PENDING);
    assertThat(file.isInUse()).isFalse();
  }

  @Test
  @DisplayName("connect: CONNECTED로 전이")
  void connectMarksInUse() {
    FileAsset file = uploadedFile();

    file.connect();

    assertThat(file.getStatus()).isEqualTo(FileStatus.CONNECTED);
    assertThat(file.isInUse()).isTrue();
  }

  @Test
  @DisplayName("disconnect: PENDING으로 전이")
  void disconnectClearsInUse() {
    FileAsset file = uploadedFile();
    file.connect();

    file.disconnect();

    assertThat(file.getStatus()).isEqualTo(FileStatus.PENDING);
    assertThat(file.isInUse()).isFalse();
  }

  @Test
  @DisplayName("delete 반복 호출: 멱등하게 처리")
  void deleteIsIdempotent() {
    FileAsset file = uploadedFile();

    file.delete();
    assertThat(file.isDeleted()).isTrue();

    file.delete();
    assertThat(file.isDeleted()).isTrue();
  }
}
