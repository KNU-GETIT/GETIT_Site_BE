-- 모집 일정 스키마. 이슈 #9 에서 만든 RecruitmentSchedule 엔티티에 대응한다.
--
-- V1 과 마찬가지로 로컬(local, ddl-auto: update)에서 Hibernate 가 실제로 생성한 DDL 을
-- 옮긴 것이다. 제약 이름만 읽기 좋게 바꿨다 (제약 이름은 validate 검사 대상이 아니다).

CREATE TABLE recruitment_schedule
(
    id                 bigint      NOT NULL AUTO_INCREMENT,

    -- Generation 과 1:1. 실제 FK 제약은 걸지 않는다 (refresh_token.user_id 와 동일한 컨벤션).
    generation_id      bigint      NOT NULL,

    total_start_at     datetime(6) NOT NULL,
    total_end_at       datetime(6) NOT NULL,
    document_start_at  datetime(6) NOT NULL,
    document_end_at    datetime(6) NOT NULL,
    interview_start_at datetime(6) NOT NULL,
    -- 사용자 입력을 받지 않는다. 서비스가 total_end_at 과 항상 같은 값으로 채운다 (명세서 4.4).
    interview_end_at   datetime(6) NOT NULL,

    created_at         datetime(6) NOT NULL,
    updated_at         datetime(6) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_recruitment_schedule_generation_id (generation_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
