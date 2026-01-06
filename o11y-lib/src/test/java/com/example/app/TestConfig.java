package com.example.app;

import com.example.app.aspect.SpanAspect;
import com.example.app.util.ConfigurationUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public ConfigurationUtil configurationUtil() {
        return new ConfigurationUtil();
    }

    @Bean
    public SpanAspect spanAspect() {
        return new SpanAspect();
    }

    @Bean
    public TestService testService() {
        return new TestService();
    }

    @Bean
    public ObservabilityAutoConfiguration observabilityAutoConfiguration() {
        return new ObservabilityAutoConfiguration();
    }
}