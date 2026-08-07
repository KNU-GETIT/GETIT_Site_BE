package com.getit.domain.file.entity;

import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilePurpose {

  LECTURE_MATERIAL(50 * 1024 * 1024L, Set.of("pdf", "zip", "pptx", "docx", "hwp", "png", "jpg")),
  ASSIGNMENT(50 * 1024 * 1024L, Set.of("zip", "pdf", "png", "jpg", "ipynb", "txt")),
  PROFILE_IMAGE(5 * 1024 * 1024L, Set.of("png", "jpg", "jpeg", "webp")),
  PROJECT_THUMBNAIL(5 * 1024 * 1024L, Set.of("png", "jpg", "jpeg", "webp"));

  private final long maxSizeBytes;
  private final Set<String> allowedExtensions;

  public boolean allows(String extension) { return allowedExtensions.contains(extension.toLowerCase()); }
}
