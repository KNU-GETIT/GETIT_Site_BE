# GETIT Site — Backend

GETIT 동아리 통합 사이트 백엔드. 공개 사이트 · 부원 LMS · 운영진 어드민 세 영역을 하나의 API 서버가 담당한다.

설계 문서는 `DOCS/` 참조 — 백엔드 설계 명세서, API 명세서(102개 엔드포인트), 작업 분할 계획, 코딩 컨벤션.

## 기술 스택

| 항목 | 버전 |
|---|---|
| Java | 21 (toolchain 고정) |
| Spring Boot | 3.5.16 |
| Gradle | Wrapper 사용 (`./gradlew`) |
| DB | MySQL 8.4 / 테스트는 H2 |
| 캐시·토큰 | Redis 7 |
| 문서 | SpringDoc OpenAPI 3 |

로컬 JDK 버전은 상관없다. Gradle toolchain 이 Java 21 로 컴파일하며, 없으면 자동으로 받아온다.

## 시작하기

```bash
docker compose up -d          # MySQL · Redis 기동
./gradlew bootRun             # 기본 프로파일 = local
```

| 주소 | 용도 |
|---|---|
| http://localhost:8080 | API |
| http://localhost:8080/swagger-ui.html | Swagger UI |

```bash
./gradlew test                # 테스트 (외부 인프라 불필요, H2 사용)
./gradlew build               # 빌드 + 테스트
```

## 패키지 구조

```
com.getit
├── global                    R — 전 도메인 공통. 소유자 외 수정 금지
│   ├── config                SecurityConfig · CorsConfig · OpenApiConfig · JpaAuditingConfig
│   ├── dto                   ApiResponse · ErrorResponse · PageResponse
│   ├── entity                BaseTimeEntity · SoftDeletableEntity
│   └── exception             ErrorCode · CommonErrorCode · BusinessException · GlobalExceptionHandler
└── domain
    ├── auth                  R   OAuth2 · JWT
    ├── user                  A   사용자 · 그룹
    ├── recruitment           A   기수 · 지원서 · 평가
    ├── dashboard             A   운영진 통계 (조립만)
    ├── setting               A/B 하위 패키지 단위로 분할
    │   ├── generation        A
    │   ├── curriculum        A
    │   ├── staff             A
    │   ├── home              A
    │   ├── category          B
    │   ├── event             B
    │   ├── faq               B
    │   └── feature           B
    ├── lecture               B   분류 · 강의 · 과제 · 제출 · 피드백
    ├── qna                   B   질문 · 답변
    ├── project               B   프로젝트 쇼케이스
    └── file                  B   공통 파일 업로드
```

**패키지 = 소유권.** 자기 패키지 밖의 파일은 수정하지 않고 소유자에게 요청한다.
각자 Controller → Service → Repository → Entity 를 자기 패키지 안에서 끝낸다.

### 절대 건드리지 않는 파일 (작업 분할 계획 4.1)

| 파일 | 소유 |
|---|---|
| `global/config/SecurityConfig.java` | R — 경로 규칙 추가는 R 에게 요청 |
| `src/main/resources/application*.yml` | R — 설정 추가는 R 에게 요청 |
| `global/dto/*`, `global/exception/GlobalExceptionHandler.java` | R — 수정 금지 |
| `User` · `Generation` 엔티티 | A — B 는 읽기만, 필드 추가는 A 에게 요청 |

### 크로스 도메인 참조 (작업 분할 계획 4.2)

다른 도메인의 Repository 를 직접 참조하지 않는다. **제공자 패키지에 인터페이스를 두고 소비자가 주입받는다.**
`UserQueryService`(A 제공), `LectureStatService` · `QuestionStatService` · `EventQueryService` · `HomeContentProvider`(B 제공).

## 공통 규약

### 응답 envelope

모든 응답은 `ApiResponse<T>` 로 감싼다. (API 명세서 0.2)

```json
{ "success": true,  "data": { }, "error": null }
{ "success": false, "data": null, "error": { "code": "APPLICATION_DEADLINE_PASSED", "message": "..." } }
```

`fieldErrors` 는 `@Valid` 검증 실패(`VALIDATION_FAILED`) 시에만 포함된다.
`null` 은 전역으로 생략하지 않는다 — 명세서가 `null` 을 의미 있는 값으로 쓴다 (`totalScore: null` = 미평가).

### ErrorCode

한 enum 에 몰면 PR 마다 충돌하므로 도메인별 파일로 쪼갠다. `global.exception.ErrorCode` 인터페이스를 구현하면 된다.

```java
public enum RecruitmentErrorCode implements ErrorCode { ... }
```

### 시간

서버 타임존은 `Asia/Seoul` 고정 (JVM · Jackson · Hibernate JDBC 3중). D-day 는 항상 서버에서 계산해 내려준다.

## 브랜치 · 커밋

```
브랜치   feat/{이슈번호}-{작업내용}     예) feat/12-application-submit
커밋     feat(recruitment): 지원서 임시저장 API 구현
PR       500줄 이하. 교차 리뷰 1 approve 로 머지. main 직접 push 금지
```

Branch type: `feat` · `fix` · `refactor` · `chore` — 1 Issue 1 Branch, PR 본문에 `close #이슈번호`.

## 현재 상태

`global` 공통 인프라와 패키지 골격만 구성된 상태다. 각 도메인 패키지는 비어 있다.

> ⚠️ **`SecurityConfig` 는 임시 설정이다.** JWT 필터가 아직 없어 local · dev 는 전 경로 permitAll 이고,
> prod 만 `/api/public/**` 외 401 을 반환한다. auth 작업에서 명세서 1.1 의 권한 규칙으로 교체한다.
