package com.getit.domain.file.service;

import java.util.List;

public interface FileQueryService {

  FileInfo findById(Long fileId);

  List<FileInfo> findAllByIds(List<Long> fileIds);
}
