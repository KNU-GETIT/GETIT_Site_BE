package com.getit.domain.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.getit.domain.file.dto.FileUploadResponse;
import com.getit.domain.file.entity.FileAsset;
import com.getit.domain.file.entity.FilePurpose;
import com.getit.domain.file.exception.FileErrorCode;
import com.getit.domain.file.repository.FileAssetRepository;
import com.getit.domain.file.storage.FileStorage;
import com.getit.domain.user.entity.Role;
import com.getit.global.exception.BusinessException;
import com.getit.global.exception.CommonErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @Mock
  private FileStorage fileStorage;

  @Mock
  private FileAssetRepository fileAssetRepository;

  @InjectMocks
  private FileService fileService;

  private static final Long UPLOADER_ID = 1L;

  @Nested
  @DisplayName("upload")
  class Upload {

    @Test
    @DisplayName("정책 준수: 정상 업로드")
    void uploadsValidFile() {
      MockMultipartFile file =
          new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
      when(fileStorage.upload(any(), anyString())).thenReturn("http://localhost:8080/api/public/files/x.pdf");
      when(fileAssetRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

      FileUploadResponse response = fileService.upload(file, FilePurpose.ASSIGNMENT, UPLOADER_ID);

      assertThat(response.originalName()).isEqualTo("report.pdf");
      assertThat(response.url()).isEqualTo("http://localhost:8080/api/public/files/x.pdf");
    }

    @Test
    @DisplayName("확장자 불허: 예외 발생")
    void rejectsInvalidExtension() {
      MockMultipartFile file =
          new MockMultipartFile("file", "malware.exe", "application/octet-stream", "content".getBytes());

      assertThatThrownBy(() -> fileService.upload(file, FilePurpose.ASSIGNMENT, UPLOADER_ID))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.INVALID_FILE_EXTENSION);
    }

    @Test
    @DisplayName("확장자 없음: 예외 발생")
    void rejectsMissingExtension() {
      MockMultipartFile file =
          new MockMultipartFile("file", "noextension", "text/plain", "content".getBytes());

      assertThatThrownBy(() -> fileService.upload(file, FilePurpose.ASSIGNMENT, UPLOADER_ID))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.INVALID_FILE_EXTENSION);
    }

    @Test
    @DisplayName("용량 초과: 예외 발생")
    void rejectsOversizedFile() {
      byte[] tooBig = new byte[6 * 1024 * 1024];
      MockMultipartFile file =
          new MockMultipartFile("file", "profile.png", "image/png", tooBig);

      assertThatThrownBy(() -> fileService.upload(file, FilePurpose.PROFILE_IMAGE, UPLOADER_ID))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.INVALID_FILE_SIZE);
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("파일 없음: 예외 발생")
    void throwsWhenNotFound() {
      when(fileAssetRepository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> fileService.delete(1L, UPLOADER_ID, Role.MEMBER))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 삭제됨: 예외 발생")
    void throwsWhenAlreadyDeleted() {
      FileAsset file = uploadedFile();
      file.delete();
      when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

      assertThatThrownBy(() -> fileService.delete(1L, UPLOADER_ID, Role.MEMBER))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("권한 없음: 예외 발생")
    void throwsWhenNotOwnerNorAdmin() {
      FileAsset file = uploadedFile();
      when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

      assertThatThrownBy(() -> fileService.delete(1L, 999L, Role.MEMBER))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommonErrorCode.NOT_RESOURCE_OWNER);
    }

    @Test
    @DisplayName("사용 중(FILE_IN_USE): 예외 발생")
    void throwsWhenInUse() {
      FileAsset file = uploadedFile();
      file.connect();
      when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

      assertThatThrownBy(() -> fileService.delete(1L, UPLOADER_ID, Role.MEMBER))
          .isInstanceOf(BusinessException.class)
          .hasFieldOrPropertyWithValue("errorCode", FileErrorCode.FILE_IN_USE);
    }

    @Test
    @DisplayName("본인 소유: 정상 삭제")
    void deletesAsOwner() {
      FileAsset file = uploadedFile();
      when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

      fileService.delete(1L, UPLOADER_ID, Role.MEMBER);

      assertThat(file.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("ADMIN 대리: 정상 삭제")
    void deletesAsAdmin() {
      FileAsset file = uploadedFile();
      when(fileAssetRepository.findById(1L)).thenReturn(Optional.of(file));

      fileService.delete(1L, 999L, Role.ADMIN);

      assertThat(file.isDeleted()).isTrue();
    }
  }

  private FileAsset uploadedFile() {
    return FileAsset.upload("key.txt", "original.txt", "http://localhost/x.txt", 10L, "text/plain", UPLOADER_ID);
  }
}
