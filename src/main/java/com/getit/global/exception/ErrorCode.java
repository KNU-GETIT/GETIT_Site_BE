package com.getit.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인별 ErrorCode enum 이 구현하는 계약.
 *
 * <p>하나의 enum 에 전 도메인 코드를 몰면 PR 마다 충돌하므로 도메인별 파일로 쪼갠다.
 * (작업 분할 계획 4.3)
 *
 * <pre>
 * global      : CommonErrorCode
 * recruitment : RecruitmentErrorCode
 * lecture     : LectureErrorCode
 * ...
 * </pre>
 */
public interface ErrorCode {

  String getCode();

  String getMessage();

  HttpStatus getStatus();
}
