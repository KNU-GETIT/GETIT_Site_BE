package com.getit.domain.recruitment.repository;

import com.getit.domain.recruitment.entity.RecruitmentSchedule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentScheduleRepository extends JpaRepository<RecruitmentSchedule, Long> {

  /** 6.1 · 6.2 는 경로 파라미터 없이 활성 기수 기준으로 동작한다. */
  Optional<RecruitmentSchedule> findByGenerationId(Long generationId);
}
