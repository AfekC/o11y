package com.example.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class O11yDefaultsEnvironmentPostProcessorTest {

    private final O11yDefaultsEnvironmentPostProcessor processor = new O11yDefaultsEnvironmentPostProcessor();

    @Test
    void addsLibraryDefaults() {
        var env = new StandardEnvironment();
        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("management.endpoints.web.exposure.include")).isEqualTo("health,info,prometheus");
        assertThat(env.getProperty("management.tracing.sampling.probability")).isEqualTo("1.0");
        assertThat(env.getProperty("management.opentelemetry.tracing.export.otlp.transport")).isEqualTo("grpc");
    }

    @Test
    void appConfigOverridesTheDefaults() {
        var env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("app",
                Map.of("management.tracing.sampling.probability", "0.1")));
        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("management.tracing.sampling.probability")).isEqualTo("0.1");
        assertThat(env.getPropertySources().stream().reduce((a, b) -> b).orElseThrow().getName())
                .isEqualTo(O11yDefaultsEnvironmentPostProcessor.NAME);
    }

    @Test
    void isIdempotent() {
        var env = new StandardEnvironment();
        processor.postProcessEnvironment(env, new SpringApplication());
        int count = env.getPropertySources().size();
        processor.postProcessEnvironment(env, new SpringApplication());
        assertThat(env.getPropertySources().size()).isEqualTo(count);
    }
}
