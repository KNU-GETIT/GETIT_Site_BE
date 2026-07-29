package com.getit.global.schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 마이그레이션과 엔티티가 어긋나지 않는지 실제 MySQL 로 검증한다.
 *
 * <p>빈 DB 에 Flyway 를 돌린 뒤 {@code ddl-auto: validate} 로 컨텍스트를 띄운다.
 * 엔티티에 필드를 추가하고 마이그레이션을 안 쓰면 <b>여기서 기동이 실패한다.</b>
 * 그 상태로 머지되면 dev 배포에서야 발견된다.
 *
 * <p>H2 로는 검증할 수 없다. char(64) · datetime(6) · bit(1) 같은 타입이 MySQL 과 다르게 잡힌다.
 * 그래서 기본 {@code test} 에서 제외하고 {@code schemaTest} 로 분리했다.
 *
 * <pre>
 *   docker compose up -d
 *   ./gradlew schemaTest
 * </pre>
 */
@Tag("schema")
@SpringBootTest(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.clean-disabled=false",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.datasource.url=${schema.test.url}",
    "spring.datasource.username=${schema.test.username}",
    "spring.datasource.password=${schema.test.password}",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver"
})
class SchemaMigrationTest {

  @Autowired
  private DataSource dataSource;

  /**
   * 컨텍스트가 떴다는 것 자체가 검증이다.
   * Flyway 가 스키마를 만들고 Hibernate 가 엔티티와 대조해 통과했다는 뜻이다.
   */
  @Test
  @DisplayName("마이그레이션으로 만든 스키마가 엔티티와 일치한다")
  void migrationMatchesEntities() {
    assertThat(dataSource).isNotNull();
  }

  @Test
  @DisplayName("마이그레이션이 실제로 적용되었다")
  void migrationHistoryExists() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);

    Integer applied = jdbc.queryForObject(
        "select count(*) from flyway_schema_history where success = true", Integer.class);

    assertThat(applied).isNotNull().isPositive();
  }

  @Test
  @DisplayName("학번 형식이 DB 에서 강제된다")
  void studentNumberFormatIsEnforced() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    jdbc.update("delete from users where email = 'ck-probe@getit.com'");

    String insert = """
        insert into users
          (email, provider_id, name, student_number, role, status, created_at, updated_at)
        values ('ck-probe@getit.com', 'ck-probe', '김학번', ?, 'GUEST', 'ACTIVE', now(6), now(6))
        """;

    // char(10) 만으로는 'abc' 도 '' 도 그대로 들어간다. CHECK 제약이 이를 막는다.
    //
    // Spring 은 MySQL 의 CHECK 위반(3819)을 DataIntegrityViolationException 으로 변환하지 않고
    // UncategorizedSQLException 으로 남긴다. 타입 대신 제약 이름으로 확인한다.
    for (String invalid : new String[] {"abc", "12345", "20211100AB"}) {
      assertThatThrownBy(() -> jdbc.update(insert, invalid))
          .isInstanceOf(DataAccessException.class)
          .hasMessageContaining("ck_users_student_number");
    }

    assertThatCode(() -> jdbc.update(insert, "2021110000")).doesNotThrowAnyException();
    jdbc.update("delete from users where email = 'ck-probe@getit.com'");

    // 지원서를 내기 전 GUEST 는 학번이 비어 있다. NULL 은 허용해야 한다.
    assertThatCode(() -> jdbc.update(insert, (Object) null)).doesNotThrowAnyException();
    jdbc.update("delete from users where email = 'ck-probe@getit.com'");
  }

  @Test
  @DisplayName("enum 컬럼이 네이티브 ENUM 이 아니라 varchar 다")
  void enumColumnsAreVarchar() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);

    assertThat(jdbc.queryForList(
        "select column_type from information_schema.columns "
            + "where table_schema = database() and table_name = 'users' "
            + "and column_name in ('role', 'status')", String.class))
        .as("네이티브 ENUM 을 쓰면 값을 추가할 때마다 ALTER TABLE 이 필요해진다")
        .isNotEmpty()
        .allSatisfy(type -> assertThat(type).startsWith("varchar"));
  }
}
