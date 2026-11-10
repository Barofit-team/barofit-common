package com.barofit.testsupport;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 모든 서비스 레포가 테스트에서 재사용하는 공유 PostgreSQL 컨테이너.
 *
 * <p>이미지 태그는 {@code barofit.test.postgres-image} 시스템 프로퍼티로 주입한다 (각 레포 build.gradle의 {@code
 * jvmArgumentProviders}에서 설정). JVM당 하나의 컨테이너만 뜨므로 테스트 클래스마다 새로 기동하지 않는다.
 */
public final class BarofitTestPostgres {

  private static final String IMAGE_PROPERTY = "barofit.test.postgres-image";

  private static final PostgreSQLContainer<?> CONTAINER = start();

  private BarofitTestPostgres() {}

  public static PostgreSQLContainer<?> container() {
    return CONTAINER;
  }

  private static PostgreSQLContainer<?> start() {
    String image = System.getProperty(IMAGE_PROPERTY);
    if (image == null || image.isBlank()) {
      throw new IllegalStateException(IMAGE_PROPERTY + " 시스템 프로퍼티가 없습니다. 테스트는 Gradle로 실행하세요");
    }
    PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(image));
    container.start();
    return container;
  }
}
