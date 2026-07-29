package com.getit.domain.setting.generation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.getit.domain.setting.generation.entity.Generation;
import com.getit.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class GenerationRepositoryTest {

  @Autowired
  private GenerationRepository generationRepository;

  @Test
  @DisplayName("활성 기수만 조회한다")
  void findsOnlyActiveGeneration() {
    generationRepository.save(Generation.create(8, 2025));
    Generation active = Generation.create(9, 2026);
    active.activate();
    generationRepository.save(active);

    assertThat(generationRepository.findByIsActiveTrue())
        .isPresent()
        .get()
        .extracting(Generation::getGenerationNo)
        .isEqualTo(9);
  }

  @Test
  @DisplayName("활성 기수가 없으면 빈 Optional 을 반환한다")
  void returnsEmptyWhenNoActiveGeneration() {
    generationRepository.save(Generation.create(8, 2025));

    assertThat(generationRepository.findByIsActiveTrue()).isEmpty();
  }

  @Test
  @DisplayName("기수 번호로 조회하고 존재 여부를 확인한다")
  void findsByGenerationNo() {
    generationRepository.save(Generation.create(9, 2026));

    assertThat(generationRepository.findByGenerationNo(9)).isPresent();
    assertThat(generationRepository.existsByGenerationNo(9)).isTrue();
    assertThat(generationRepository.existsByGenerationNo(99)).isFalse();
  }

  @Test
  @DisplayName("기수 번호가 중복되면 저장에 실패한다")
  void rejectsDuplicateGenerationNo() {
    generationRepository.saveAndFlush(Generation.create(9, 2026));

    assertThatThrownBy(() -> generationRepository.saveAndFlush(Generation.create(9, 2027)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
