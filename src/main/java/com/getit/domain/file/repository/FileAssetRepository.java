package com.getit.domain.file.repository;

import com.getit.domain.file.entity.FileAsset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {

  List<FileAsset> findAllByIdIn(List<Long> ids);
}
