package com.getit.domain.auth.controller;

import com.getit.domain.auth.dto.MeResponse;
import com.getit.domain.auth.security.CustomUserDetails;
import com.getit.domain.auth.service.AuthService;
import com.getit.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "내 프로필 조회", description = "명세서 1.5")
  @GetMapping("/me")
  public ApiResponse<MeResponse> getMe(@AuthenticationPrincipal CustomUserDetails principal) {
    return ApiResponse.success(authService.getMe(principal.getUserId()));
  }
}
