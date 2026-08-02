package com.getit.domain.recruitment.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 모집 일정 저장 요청. (API 명세서 6.2)
 *
 * <p>interviewEndAt 은 요청으로 받지 않는다. 서버가 totalEndAt 으로 채운다.
 */
public record RecruitmentScheduleUpdateRequest(
    @NotNull LocalDateTime totalStartAt,
    @NotNull LocalDateTime totalEndAt,
    @NotNull LocalDateTime documentStartAt,
    @NotNull LocalDateTime documentEndAt,
    @NotNull LocalDateTime interviewStartAt
) { }
