package com.getit;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GetitApplication {

  public static void main(String[] args) {
    SpringApplication.run(GetitApplication.class, args);
  }

  /**
   * D-day · 마감 검증이 전부 서버 시각 기준이므로 JVM 기본 타임존까지 KST로 고정한다.
   * (jackson.time-zone / hibernate.jdbc.time_zone 은 application.yml 에서 별도 지정)
   */
  @PostConstruct
  void initTimeZone() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
  }
}
