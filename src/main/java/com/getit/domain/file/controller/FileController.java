package com.getit.domain.file.controller;

import com.getit.domain.auth.security.CustomUserDetails;
import com.getit.domain.file.dto.FileUploadResponse;
import com.getit.domain.file.entity.FilePurpose;
import com.getit.domain.file.service.FileService;
import com.getit.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File", description = "공통 파일 업로드")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;

  @Operation(summary = "Multipart 직접 업로드", description = "명세서 13.2")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<FileUploadResponse> upload(
      @RequestPart MultipartFile file,
      @RequestParam FilePurpose purpose,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    return ApiResponse.success(fileService.upload(file, purpose, principal.getUserId()));
  }

  @Operation(summary = "파일 삭제", description = "명세서 13.3")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    fileService.delete(id, principal.getUserId(), principal.getRole());
  }
}
