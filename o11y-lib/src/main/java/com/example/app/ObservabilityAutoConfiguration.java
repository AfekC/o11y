package com.example.app;


import com.example.app.aspect.SpanAspect;
import com.example.app.util.ConfigurationUtil;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;


import java.net.URI;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * Auto-configured in any Spring Boot app, whatever its base package (registered in
 * META-INF/spring/...AutoConfiguration.imports, so it does not rely on component scanning).
 * The /metrics redirect is only added in servlet web apps, so non-web apps (workers, consumers)
 * can use the library without Spring MVC on the classpath.
 */
@AutoConfiguration
public class ObservabilityAutoConfiguration {


    @Value("${service.name:unknown}")
    private String serviceName;

    @Value("${environment:unknown}")
    private String environment;

    @Value("${system:unknown}")
    private String system;



    /** Registers the target.info gauge once every singleton (incl. the MeterRegistry) exists. */
    @Bean
    public SmartInitializingSingleton targetInfoMetric(ConfigurationUtil configurationUtil) {
        return () -> configurationUtil.addMetric("target.info", 1,
                "service.name", this.serviceName,
                "environment", this.environment,
                "system", this.system
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationUtil configurationUtil() {
        return new ConfigurationUtil();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpanAspect spanAspect() {
        return new SpanAspect();
    }

    @Bean
    public Resource openTelemetryResource() {
        Attributes customAttributes = Attributes.of(
                AttributeKey.stringKey("service.name"), this.serviceName,
                AttributeKey.stringKey("environment"), this.environment,
                AttributeKey.stringKey("system"), this.system
        );

        return Resource.getDefault()
                .merge(Resource.create(customAttributes));
    }

    /**
     * Redirects requests from /metrics to /actuator/prometheus.
     * This ensures compatibility with legacy systems expecting /metrics.
     * Nested and conditional: its method signatures reference Spring MVC types, which must not be
     * loaded in non-web apps.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.function.RouterFunction")
    static class MetricsRedirectConfiguration {

        @Bean
        public RouterFunction<ServerResponse> redirectMetrics() {
            return route(GET("/metrics"),
                    req -> ServerResponse.permanentRedirect(URI.create("/actuator/prometheus")).build()
            );
        }
    }
}
