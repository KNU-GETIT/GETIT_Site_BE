package com.getit.global.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * 목록 API 공통 래퍼. (API 명세서 0.3)
 * page 는 0부터 시작한다.
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast()
    );
  }

  /** 엔티티 Page 를 응답 DTO 로 변환하면서 감쌀 때 사용한다. */
  public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
    return from(page.map(mapper));
  }
}
