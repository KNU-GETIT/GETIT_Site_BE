package com.getit.domain.recruitment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecruitmentScheduleTest {

  @Test
  @DisplayName("생성 시 interviewEndAt 은 totalEndAt 과 동일하게 채워진다")
  void createsWithInterviewEndAtSyncedToTotalEndAt() {
    LocalDateTime totalStartAt = LocalDateTime.of(2026, 9, 1, 0, 0);
    LocalDateTime totalEndAt = LocalDateTime.of(2026, 9, 30, 23, 59, 59);
    LocalDateTime documentStartAt = LocalDateTime.of(2026, 9, 1, 0, 0);
    LocalDateTime documentEndAt = LocalDateTime.of(2026, 9, 10, 23, 59, 59);
    LocalDateTime interviewStartAt = LocalDateTime.of(2026, 9, 15, 0, 0);

    RecruitmentSchedule schedule = RecruitmentSchedule.create(
        1L, totalStartAt, totalEndAt, documentStartAt, documentEndAt, interviewStartAt);

    assertThat(schedule.getGenerationId()).isEqualTo(1L);
    assertThat(schedule.getTotalStartAt()).isEqualTo(totalStartAt);
    assertThat(schedule.getTotalEndAt()).isEqualTo(totalEndAt);
    assertThat(schedule.getDocumentStartAt()).isEqualTo(documentStartAt);
    assertThat(schedule.getDocumentEndAt()).isEqualTo(documentEndAt);
    assertThat(schedule.getInterviewStartAt()).isEqualTo(interviewStartAt);
    assertThat(schedule.getInterviewEndAt()).isEqualTo(totalEndAt);
  }

  @Test
  @DisplayName("수정 시에도 interviewEndAt 은 새 totalEndAt 으로 재동기화된다")
  void updateResyncsInterviewEndAtToNewTotalEndAt() {
    RecruitmentSchedule schedule = RecruitmentSchedule.create(
        1L,
        LocalDateTime.of(2026, 9, 1, 0, 0),
        LocalDateTime.of(2026, 9, 30, 23, 59, 59),
        LocalDateTime.of(2026, 9, 1, 0, 0),
        LocalDateTime.of(2026, 9, 10, 23, 59, 59),
        LocalDateTime.of(2026, 9, 15, 0, 0));

    LocalDateTime newTotalEndAt = LocalDateTime.of(2026, 10, 15, 23, 59, 59);
    schedule.update(
        LocalDateTime.of(2026, 9, 5, 0, 0),
        newTotalEndAt,
        LocalDateTime.of(2026, 9, 5, 0, 0),
        LocalDateTime.of(2026, 9, 15, 23, 59, 59),
        LocalDateTime.of(2026, 9, 20, 0, 0));

    assertThat(schedule.getTotalEndAt()).isEqualTo(newTotalEndAt);
    assertThat(schedule.getInterviewEndAt()).isEqualTo(newTotalEndAt);
  }
}
