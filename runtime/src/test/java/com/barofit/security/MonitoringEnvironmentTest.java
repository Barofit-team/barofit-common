package com.barofit.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsProperties;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

class MonitoringEnvironmentTest {
  @Test
  void histogram_environment_variable_binds_to_the_http_timer_map() {
    var properties =
        Binder.get(
                environment(
                    Map.of(
                        "MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILESHISTOGRAM_HTTP_SERVER_REQUESTS",
                        "true")))
            .bind("management.metrics", Bindable.of(MetricsProperties.class))
            .get();
    assertThat(properties.getDistribution().getPercentilesHistogram())
        .containsEntry("http.server.requests", true);
  }

  @Test
  void health_alias_environment_variable_enables_application_port_probes() {
    var environment =
        environment(Map.of("MANAGEMENT_ENDPOINT_HEALTH_PROBES_ADDADDITIONALPATHS", "true"));
    ConfigurationPropertySources.attach(environment);
    assertThat(
            environment.getProperty(
                "management.endpoint.health.probes.add-additional-paths", Boolean.class))
        .isTrue();
  }

  @Test
  void datasource_environment_variables_limit_connection_pool() {
    var properties =
        Binder.get(
                environment(
                    Map.of(
                        "SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE", "4",
                        "SPRING_DATASOURCE_HIKARI_MINIMUMIDLE", "1")))
            .bind("spring.datasource.hikari", Bindable.of(HikariConfig.class))
            .get();
    assertThat(properties.getMaximumPoolSize()).isEqualTo(4);
    assertThat(properties.getMinimumIdle()).isEqualTo(1);
  }

  private static StandardEnvironment environment(Map<String, Object> values) {
    var environment = new StandardEnvironment();
    environment
        .getPropertySources()
        .replace(
            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            new SystemEnvironmentPropertySource("systemEnvironment", values));
    return environment;
  }
}
