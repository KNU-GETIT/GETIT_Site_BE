package com.getit.domain.file.service;

public interface FileConnectionService {

  void connect(Long fileId);

  void disconnect(Long fileId);
}
