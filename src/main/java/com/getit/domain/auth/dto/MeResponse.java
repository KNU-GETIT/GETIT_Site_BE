package com.getit.domain.auth.dto;

import com.getit.domain.user.entity.Role;
import com.getit.domain.user.entity.User;
import com.getit.domain.user.entity.UserStatus;

/**
 * 내 프로필 응답. (API 명세서 1.5)
 *
 * <p>필드명과 순서를 명세서 JSON 그대로 맞췄다. 프론트가 앱 진입 시 이 응답으로 권한을 판단한다.
 *
 * <p>명세서 1.5 에는 roleLabel · statusLabel 이 없어 넣지 않았다. 목록 API(9.1)와 다른 점이다.
 * 화면에 권한을 표기해야 한다면 추가할 수 있다.
 */
public record MeResponse(
    Long id,
    String email,
    String name,
    String phoneNumber,
    String college,
    String major,
    Integer studentYear,
    String studentNumber,
    String profileImageUrl,
    Role role,
    Integer generationNo,
    UserStatus status
) {

  public static MeResponse from(User user) {
    return new MeResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getPhoneNumber(),
        user.getCollege(),
        user.getMajor(),
        user.getStudentYear(),
        user.getStudentNumber(),
        user.getProfileImageUrl(),
        user.getRole(),
        user.getGenerationNo(),
        user.getStatus()
    );
  }
}
