package com.getit.domain.file.service;

import com.getit.domain.file.entity.FileAsset;
import com.getit.domain.file.exception.FileErrorCode;
import com.getit.domain.file.repository.FileAssetRepository;
import com.getit.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FileConnectionServiceImpl implements FileConnectionService {

  private final FileAssetRepository fileAssetRepository;

  @Override
  public void connect(Long fileId) { findFile(fileId).connect(); }

  @Override
  public void disconnect(Long fileId) { findFile(fileId).disconnect(); }

  private FileAsset findFile(Long fileId) {
    return fileAssetRepository.findById(fileId)
        .orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));
  }
}
