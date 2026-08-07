package com.getit.domain.recruitment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.getit.domain.recruitment.entity.RecruitmentSchedule;
import com.getit.global.config.JpaAuditingConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class RecruitmentScheduleRepositoryTest {

  @Autowired
  private RecruitmentScheduleRepository recruitmentScheduleRepository;

  private RecruitmentSchedule schedule(Long generationId) {
    return RecruitmentSchedule.create(
        generationId,
        LocalDateTime.of(2026, 9, 1, 0, 0),
        LocalDateTime.of(2026, 9, 30, 23, 59, 59),
        LocalDateTime.of(2026, 9, 1, 0, 0),
        LocalDateTime.of(2026, 9, 10, 23, 59, 59),
        LocalDateTime.of(2026, 9, 15, 0, 0));
  }

  @Test
  @DisplayName("기수 ID로 조회한다")
  void findsByGenerationId() {
    recruitmentScheduleRepository.save(schedule(1L));

    assertThat(recruitmentScheduleRepository.findByGenerationId(1L))
        .isPresent()
        .get()
        .extracting(RecruitmentSchedule::getGenerationId)
        .isEqualTo(1L);
  }

  @Test
  @DisplayName("해당 기수의 일정이 없으면 빈 Optional 을 반환한다")
  void returnsEmptyWhenScheduleNotFound() {
    assertThat(recruitmentScheduleRepository.findByGenerationId(999L)).isEmpty();
  }

  @Test
  @DisplayName("한 기수에 일정이 두 개 저장되면 실패한다")
  void rejectsDuplicateGenerationId() {
    recruitmentScheduleRepository.saveAndFlush(schedule(1L));

    assertThatThrownBy(() -> recruitmentScheduleRepository.saveAndFlush(schedule(1L)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
