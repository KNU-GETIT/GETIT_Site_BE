package com.getit.domain.file.service;

import com.getit.domain.file.entity.FileAsset;
import com.getit.domain.file.exception.FileErrorCode;
import com.getit.domain.file.repository.FileAssetRepository;
import com.getit.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileQueryServiceImpl implements FileQueryService {

  private final FileAssetRepository fileAssetRepository;

  @Override
  public FileInfo findById(Long fileId) {
    FileAsset file = fileAssetRepository.findById(fileId)
        .orElseThrow(() -> new BusinessException(FileErrorCode.FILE_NOT_FOUND));
    return FileInfo.from(file);
  }

  @Override
  public List<FileInfo> findAllByIds(List<Long> fileIds) {
    return fileAssetRepository.findAllByIdIn(fileIds).stream()
        .map(FileInfo::from)
        .toList();
  }
}
