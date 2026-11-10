package com.barofit.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 기본 스키마를 쓰는 서비스(identity/routine/workout 등)용 테스트 설정. {@link BarofitTestPostgres}의 공유 컨테이너를
 * {@code @ServiceConnection}으로 연결해 datasource 프로퍼티를 자동 구성한다.
 *
 * <p>analytics/report처럼 별도 스키마(currentSchema)가 필요한 서비스는 이 설정 대신 {@link
 * BarofitTestPostgres#container()}를 직접 참조해 {@code @DynamicPropertySource}로 스키마를 포함한 JDBC URL을 구성한다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class BarofitPostgresTestSupport {

  @Bean(destroyMethod = "")
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return BarofitTestPostgres.container();
  }
}
