package com.getit.domain.auth.service;

import com.getit.domain.auth.dto.MeResponse;
import com.getit.domain.user.entity.User;
import com.getit.domain.user.exception.UserErrorCode;
import com.getit.domain.user.repository.UserRepository;
import com.getit.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 사용자 조회.
 *
 * <p>Coding Convention 에 따라 인터페이스를 두지 않고 구현을 클래스 안에 그대로 작성한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final UserRepository userRepository;

  /**
   * 내 프로필 조회. (1.5 GET /api/auth/me)
   *
   * <p>토큰은 유효한데 사용자가 없는 경우가 있다. 탈퇴 처리된 뒤 만료 전 토큰으로 접근하는 상황이다.
   * soft delete 된 사용자는 조회되지 않으므로 404 로 떨어진다.
   */
  public MeResponse getMe(Long userId) {
    User user = userRepository.findById(userId)
        .filter(found -> !found.isDeleted())
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    return MeResponse.from(user);
  }
}
