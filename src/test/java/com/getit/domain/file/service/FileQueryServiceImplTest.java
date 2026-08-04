package com.getit.domain.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.getit.domain.file.entity.FileAsset;
import com.getit.domain.file.exception.FileErrorCode;
import com.getit.domain.file.repository.FileAssetRepository;
import com.getit.global.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileQueryServiceImplTest {

  @Mock
  private FileAssetRepository fileAssetRepository;

  @InjectMocks
  private FileQueryServiceImpl fileQueryService;

  private FileAsset uploadedFile() {
    return FileAsset.upload("key.txt", "original.txt", "http://localhost/x.txt", 10L, "text/plain", 1L);
  }

  @Test
  @DisplayName("존재하는 파일: FileInfo 반환")
  void returnsFileInfo() {
    FileAsset file = uploadedFile();
    when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

    FileInfo info = fileQueryService.findById(1L);

    assertThat(info.url()).isEqualTo("http://localhost/x.txt");
    assertThat(info.originalName()).isEqualTo("original.txt");
    assertThat(info.size()).isEqualTo(10L);
  }

  @Test
  @DisplayName("존재하지 않는 파일: 예외 발생")
  void throwsWhenNotFound() {
    when(fileAssetRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> fileQueryService.findById(1L))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.FILE_NOT_FOUND);
  }

  @Test
  @DisplayName("여러 fileId: 한 번에 조회")
  void findsAllByIds() {
    FileAsset file = uploadedFile();
    when(fileAssetRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(file));

    List<FileInfo> infos = fileQueryService.findAllByIds(List.of(1L));

    assertThat(infos).hasSize(1);
    assertThat(infos.get(0).originalName()).isEqualTo("original.txt");
  }
}
