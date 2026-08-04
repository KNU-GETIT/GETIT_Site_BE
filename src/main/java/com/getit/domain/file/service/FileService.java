package com.getit.domain.file.service;

import com.getit.domain.file.dto.FileUploadResponse;
import com.getit.domain.file.entity.FileAsset;
import com.getit.domain.file.entity.FilePurpose;
import com.getit.domain.file.exception.FileErrorCode;
import com.getit.domain.file.repository.FileAssetRepository;
import com.getit.domain.file.storage.FileStorage;
import com.getit.domain.user.entity.Role;
import com.getit.global.exception.BusinessException;
import com.getit.global.exception.CommonErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

  private final FileStorage fileStorage;
  private final FileAssetRepository fileAssetRepository;

  public FileUploadResponse upload(MultipartFile file, FilePurpose purpose, Long uploaderId) {
    String extension = extensionOf(file.getOriginalFilename());
    if (!purpose.allows(extension)) {
      throw new BusinessException(FileErrorCode.INVALID_FILE_EXTENSION);
    }
    if (file.getSize() > purpose.getMaxSizeBytes()) {
      throw new BusinessException(FileErrorCode.INVALID_FILE_SIZE);
    }

    String key = UUID.randomUUID() + "." + extension;
    String url = fileStorage.upload(file, key);

    FileAsset saved = fileAssetRepository.save(
        FileAsset.upload(key, file.getOriginalFilename(), url, file.getSize(), file.getContentType(), uploaderId)
    );

    return FileUploadResponse.from(saved);
  }

  public void delete(Long fileId, Long requesterId, Role requesterRole) {
    FileAsset file = fileAssetRepository.findById(fileId)
        .orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));

    boolean isOwner = file.getUploaderId().equals(requesterId);
    if (!isOwner && requesterRole != Role.ADMIN) {
      throw new BusinessException(CommonErrorCode.NOT_RESOURCE_OWNER);
    }
    if (file.isInUse()) {
      throw new BusinessException(FileErrorCode.FILE_IN_USE);
    }

    fileStorage.delete(file.getStoredKey());
    file.delete();
  }

  private String extensionOf(String originalName) {
    String extension = StringUtils.getFilenameExtension(originalName);
    if (!StringUtils.hasText(extension)) {
      throw new BusinessException(FileErrorCode.INVALID_FILE_EXTENSION);
    }
    return extension;
  }
}
