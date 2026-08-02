package com.getit.domain.recruitment.service;

import com.getit.domain.recruitment.dto.RecruitmentScheduleResult;
import com.getit.domain.recruitment.entity.RecruitmentSchedule;
import com.getit.domain.recruitment.exception.RecruitmentErrorCode;
import com.getit.domain.recruitment.repository.RecruitmentScheduleRepository;
import com.getit.domain.setting.generation.entity.Generation;
import com.getit.domain.setting.generation.repository.GenerationRepository;
import com.getit.global.exception.BusinessException;
import com.getit.global.exception.CommonErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 모집 일정 조회 · 설정. (API 명세서 6.1 · 6.2) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentScheduleService {

  private final RecruitmentScheduleRepository recruitmentScheduleRepository;
  private final GenerationRepository generationRepository;

  public RecruitmentScheduleResult getSchedule() {
    Generation activeGeneration = findActiveGeneration();

    RecruitmentSchedule schedule = recruitmentScheduleRepository.findByGenerationId(activeGeneration.getId())
        .orElseThrow(() -> new BusinessException(RecruitmentErrorCode.SCHEDULE_NOT_FOUND));

    return RecruitmentScheduleResult.of(activeGeneration, schedule);
  }

  @Transactional
  public RecruitmentScheduleResult updateSchedule(
      LocalDateTime totalStartAt,
      LocalDateTime totalEndAt,
      LocalDateTime documentStartAt,
      LocalDateTime documentEndAt,
      LocalDateTime interviewStartAt
  ) {
    validateOrder(totalStartAt, totalEndAt, documentStartAt, documentEndAt, interviewStartAt);

    Generation activeGeneration = findActiveGeneration();

    RecruitmentSchedule schedule = recruitmentScheduleRepository.findByGenerationId(activeGeneration.getId())
        .map(existing -> {
          existing.update(totalStartAt, totalEndAt, documentStartAt, documentEndAt, interviewStartAt);
          return existing;
        })
        .orElseGet(() -> recruitmentScheduleRepository.save(
            RecruitmentSchedule.create(
                activeGeneration.getId(),
                totalStartAt, totalEndAt, documentStartAt, documentEndAt, interviewStartAt)));

    return RecruitmentScheduleResult.of(activeGeneration, schedule);
  }

  private Generation findActiveGeneration() {
    return generationRepository.findByIsActiveTrue()
        .orElseThrow(() -> new BusinessException(RecruitmentErrorCode.ACTIVE_GENERATION_NOT_FOUND));
  }

  /** API 명세서 6.2 검증 규칙. */
  private void validateOrder(
      LocalDateTime totalStartAt,
      LocalDateTime totalEndAt,
      LocalDateTime documentStartAt,
      LocalDateTime documentEndAt,
      LocalDateTime interviewStartAt
  ) {
    if (!totalStartAt.isBefore(totalEndAt)) {
      throw new BusinessException(
          CommonErrorCode.VALIDATION_FAILED, "총 모집 시작일은 종료일보다 빨라야 합니다.");
    }
    if (!documentStartAt.isBefore(documentEndAt) || documentEndAt.isAfter(totalEndAt)) {
      throw new BusinessException(
          CommonErrorCode.VALIDATION_FAILED, "서류 기간은 시작일이 종료일보다 빠르고 총 모집 기간 안에 있어야 합니다.");
    }
    if (documentEndAt.isAfter(interviewStartAt)) {
      throw new BusinessException(
          CommonErrorCode.VALIDATION_FAILED, "면접 시작일은 서류 마감일 이후여야 합니다.");
    }
  }
}
