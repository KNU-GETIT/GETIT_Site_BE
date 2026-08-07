package com.getit.domain.file.dto;

import com.getit.domain.file.entity.FileAsset;

public record FileUploadResponse(
    Long fileId,
    String originalName,
    String url,
    Long size,
    String contentType
) {

  public static FileUploadResponse from(FileAsset file) {
    return new FileUploadResponse(
        file.getId(),
        file.getOriginalName(),
        file.getUrl(),
        file.getSize(),
        file.getContentType()
    );
  }
}
