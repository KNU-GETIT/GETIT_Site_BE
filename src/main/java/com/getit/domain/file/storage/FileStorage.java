package com.getit.domain.file.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

  String upload(MultipartFile file, String key);

  void delete(String key);
}
