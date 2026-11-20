package com.barofit.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barofit.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(
    controllers = ManagementSecurityTest.MetricsProbe.class,
    properties = "management.server.port=9091")
@Import({
  SecurityConfig.class,
  ManagementSecurityTest.MetricsProbe.class,
  ManagementSecurityTest.SecurityInfrastructure.class,
  GlobalExceptionHandler.class
})
class ManagementSecurityTest {
  @Autowired MockMvc mockMvc;

  @MockitoBean(name = "barofitJwtDecoder")
  JwtDecoder decoder;

  @Test
  void management_port_allows_scraping_without_token() throws Exception {
    mockMvc
        .perform(
            get("/actuator/prometheus")
                .with(request -> {
                  request.setLocalPort(9091);
                  return request;
                }))
        .andExpect(status().isOk());
  }

  @Test
  void application_port_does_not_allow_anonymous_scraping() throws Exception {
    mockMvc
        .perform(
            get("/actuator/prometheus")
                .with(request -> {
                  request.setLocalPort(8080);
                  return request;
                }))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void application_port_denies_scraping_even_with_valid_token() throws Exception {
    when(decoder.decode("valid"))
        .thenReturn(Jwt.withTokenValue("valid").header("alg", "RS256").subject("user").build());
    mockMvc
        .perform(
            get("/actuator/prometheus")
                .header("Authorization", "Bearer valid")
                .with(request -> {
                  request.setLocalPort(8080);
                  return request;
                }))
        .andExpect(status().isForbidden());
  }

  @Test
  void application_health_aliases_remain_public() throws Exception {
    mockMvc
        .perform(
            get("/readyz")
                .with(request -> {
                  request.setLocalPort(8080);
                  return request;
                }))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            get("/livez")
                .with(request -> {
                  request.setLocalPort(8080);
                  return request;
                }))
        .andExpect(status().isOk());
  }

  @TestConfiguration(proxyBeanMethods = false)
  @EnableWebSecurity
  static class SecurityInfrastructure {}

  @RestController
  static class MetricsProbe {
    @GetMapping({"/actuator/prometheus", "/livez", "/readyz"})
    String metrics() {
      return "probe 1\n";
    }
  }
}
