package com.getit.domain.file.repository;

import com.getit.domain.file.entity.FileAsset;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {

  Optional<FileAsset> findByIdAndDeletedAtIsNull(Long id);
  List<FileAsset> findAllByIdInAndDeletedAtIsNull(List<Long> ids);

  List<FileAsset> findAllByIdIn(List<Long> ids);
}
