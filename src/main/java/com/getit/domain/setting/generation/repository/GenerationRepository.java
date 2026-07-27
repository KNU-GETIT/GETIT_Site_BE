package com.getit.domain.setting.generation.repository;

import com.getit.domain.setting.generation.entity.Generation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<Generation, Long> {

  /** 현재 진행 기수. 항상 0건 또는 1건이다. (설계 명세서 4.5) */
  Optional<Generation> findByIsActiveTrue();

  Optional<Generation> findByGenerationNo(Integer generationNo);

  boolean existsByGenerationNo(Integer generationNo);
}
