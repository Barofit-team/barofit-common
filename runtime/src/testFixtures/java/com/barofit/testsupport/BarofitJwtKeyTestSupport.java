package com.barofit.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

/** 통합 테스트용 JWT 키를 {@code barofit.jwt.*} 프로퍼티로 주입한다. */
@TestConfiguration(proxyBeanMethods = false)
public class BarofitJwtKeyTestSupport {

  public static final BarofitTestJwtKeys KEYS = BarofitTestJwtKeys.create("test-kid");

  @Bean
  DynamicPropertyRegistrar jwtKeyProperties() {
    return registry -> {
      registry.add("barofit.jwt.private-key-path", () -> KEYS.privateKeyPath().toString());
      registry.add("barofit.jwt.public-jwks-path", () -> KEYS.publicJwksPath().toString());
      registry.add("barofit.jwt.current-kid", KEYS::currentKid);
    };
  }
}
