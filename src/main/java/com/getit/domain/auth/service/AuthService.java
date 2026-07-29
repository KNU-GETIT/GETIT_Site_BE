package com.getit.domain.auth.service;

import com.getit.domain.auth.dto.MeResponse;
import com.getit.domain.user.exception.UserErrorCode;
import com.getit.domain.user.service.UserAccountService;
import com.getit.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 사용자 조회.
 *
 * <p>Coding Convention 에 따라 인터페이스를 두지 않고 구현을 클래스 안에 그대로 작성한다.
 * 사용자 데이터는 UserRepository 가 아니라 user 패키지가 제공하는 계약을 통해 읽는다
 * (작업 분할 계획 4.2).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserAccountService userAccountService;

  /**
   * 내 프로필 조회. (1.5 GET /api/auth/me)
   *
   * <p>토큰은 유효한데 사용자가 없는 경우가 있다. 탈퇴 처리된 뒤 만료 전 토큰으로 접근하는 상황이다.
   */
  public MeResponse getMe(Long userId) {
    return userAccountService.findActiveById(userId)
        .map(MeResponse::from)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
  }
}
