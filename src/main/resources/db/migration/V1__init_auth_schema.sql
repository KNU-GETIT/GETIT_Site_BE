-- 인증 기반 스키마. 이슈 #1 ~ #5 에서 만든 엔티티에 대응한다.
--
-- 이 파일은 Hibernate 가 MySQL 8.4 에 실제로 생성한 DDL 을 옮긴 것이다.
-- 손으로 쓰면 ddl-auto: validate 와 어긋나기 쉬워, 제약 이름만 읽기 좋게 바꿨다.
-- (제약 이름은 validate 검사 대상이 아니다)

CREATE TABLE users
(
    id                bigint       NOT NULL AUTO_INCREMENT,

    email             varchar(255) NOT NULL,
    -- Google 계정의 sub. OAuth 재로그인 시 사용자를 식별하는 키다.
    provider_id       varchar(100) NOT NULL,

    name              varchar(50)  NOT NULL,
    phone_number      varchar(20)  DEFAULT NULL,
    college           varchar(50)  DEFAULT NULL,
    major             varchar(50)  DEFAULT NULL,
    student_year      int          DEFAULT NULL,
    -- 학번. 년도 4자리 + 고유번호 6자리
    student_number    char(10)     DEFAULT NULL,
    profile_image_url varchar(512) DEFAULT NULL,
    -- 소속 기수. GUEST 는 아직 소속이 없으므로 NULL
    generation_no     int          DEFAULT NULL,

    -- GUEST · MEMBER · ADMIN. 네이티브 ENUM 을 쓰지 않는다.
    -- 값을 추가할 때마다 ALTER TABLE 이 필요해지기 때문이다.
    role              varchar(20)  NOT NULL,
    -- ACTIVE · WITHDRAWN
    status            varchar(20)  NOT NULL,

    created_at        datetime(6)  NOT NULL,
    updated_at        datetime(6)  NOT NULL,
    -- 지원서 · 과제 제출 · Q&A 이력 보존을 위해 행을 지우지 않는다
    deleted_at        datetime(6)  DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_provider_id (provider_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE generation
(
    id              bigint      NOT NULL AUTO_INCREMENT,

    generation_no   int         NOT NULL,
    -- year 는 SQL 예약어라 컬럼명을 분리했다
    generation_year int         NOT NULL,
    -- 전체에서 항상 1건만 true 여야 한다. 단일성은 서비스 트랜잭션이 보장한다
    is_active       bit(1)      NOT NULL,

    created_at      datetime(6) NOT NULL,
    updated_at      datetime(6) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_generation_no (generation_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE refresh_token
(
    id         bigint      NOT NULL AUTO_INCREMENT,

    -- 토큰 원문이 아니라 SHA-256 해시. DB 가 유출돼도 쓸 수 있는 토큰이 나오지 않는다
    token_hash char(64)    NOT NULL,
    user_id    bigint      NOT NULL,
    expires_at datetime(6) NOT NULL,
    -- 값이 있으면 이미 사용되었거나 로그아웃된 토큰이다.
    -- 행을 지우면 재사용 감지를 할 수 없어 NULL 여부로 표현한다
    revoked_at datetime(6) DEFAULT NULL,

    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    KEY idx_refresh_token_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
