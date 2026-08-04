package com.getit.domain.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.getit.domain.file.entity.FileAsset;
import com.getit.domain.file.entity.FileStatus;
import com.getit.domain.file.exception.FileErrorCode;
import com.getit.domain.file.repository.FileAssetRepository;
import com.getit.global.exception.BusinessException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileConnectionServiceImplTest {

  @Mock
  private FileAssetRepository fileAssetRepository;

  @InjectMocks
  private FileConnectionServiceImpl fileConnectionService;

  private FileAsset uploadedFile() {
    return FileAsset.upload("key.txt", "original.txt", "http://localhost/x.txt", 10L, "text/plain", 1L);
  }

  @Test
  @DisplayName("connect: CONNECTED로 전이")
  void connectsFile() {
    FileAsset file = uploadedFile();
    when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

    fileConnectionService.connect(1L);

    assertThat(file.getStatus()).isEqualTo(FileStatus.CONNECTED);
  }

  @Test
  @DisplayName("disconnect: PENDING으로 전이")
  void disconnectsFile() {
    FileAsset file = uploadedFile();
    file.connect();
    when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

    fileConnectionService.disconnect(1L);

    assertThat(file.getStatus()).isEqualTo(FileStatus.PENDING);
  }

  @Test
  @DisplayName("존재하지 않는 파일 연결: 예외 발생")
  void throwsWhenConnectingMissingFile() {
    when(fileAssetRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> fileConnectionService.connect(1L))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.FILE_NOT_FOUND);
  }
}
