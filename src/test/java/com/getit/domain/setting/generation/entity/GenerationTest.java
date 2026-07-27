package com.getit.domain.setting.generation.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GenerationTest {

  @Test
  @DisplayName("생성 직후에는 비활성 상태다")
  void createsInactiveGeneration() {
    Generation generation = Generation.create(9, 2026);

    assertThat(generation.getGenerationNo()).isEqualTo(9);
    assertThat(generation.getYear()).isEqualTo(2026);
    assertThat(generation.isActive()).isFalse();
  }

  @Test
  @DisplayName("기수와 연도를 변경한다")
  void updatesInfo() {
    Generation generation = Generation.create(9, 2026);

    generation.updateInfo(10, 2027);

    assertThat(generation.getGenerationNo()).isEqualTo(10);
    assertThat(generation.getYear()).isEqualTo(2027);
  }

  @Test
  @DisplayName("활성화 · 비활성화된다")
  void activatesAndDeactivates() {
    Generation generation = Generation.create(9, 2026);

    generation.activate();
    assertThat(generation.isActive()).isTrue();

    generation.deactivate();
    assertThat(generation.isActive()).isFalse();
  }
}
