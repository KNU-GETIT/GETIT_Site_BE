package com.getit.domain.recruitment.dto;

import com.getit.domain.recruitment.entity.RecruitmentSchedule;
import com.getit.domain.setting.generation.entity.Generation;
import java.time.LocalDateTime;

/** 모집 일정 조회 · 저장 결과. (API 명세서 6.1 · 6.2) */
public record RecruitmentScheduleResult(
    Long generationId,
    Integer generationNo,
    Integer year,
    LocalDateTime totalStartAt,
    LocalDateTime totalEndAt,
    LocalDateTime documentStartAt,
    LocalDateTime documentEndAt,
    LocalDateTime interviewStartAt,
    LocalDateTime interviewEndAt
) {

  public static RecruitmentScheduleResult of(Generation generation, RecruitmentSchedule schedule) {
    return new RecruitmentScheduleResult(
        generation.getId(),
        generation.getGenerationNo(),
        generation.getYear(),
        schedule.getTotalStartAt(),
        schedule.getTotalEndAt(),
        schedule.getDocumentStartAt(),
        schedule.getDocumentEndAt(),
        schedule.getInterviewStartAt(),
        schedule.getInterviewEndAt()
    );
  }
}
