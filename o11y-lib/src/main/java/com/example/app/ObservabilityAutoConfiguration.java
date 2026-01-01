package com.example.app;


import com.example.app.util.ConfigurationUtil;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;


import java.net.URI;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
public class ObservabilityAutoConfiguration {


    @Autowired
    private ConfigurationUtil configurationUtil;

    @Value("${service.name:unknown}")
    private String serviceName;

    @Value("${environment:unknown}")
    private String environment;

    @Value("${system:unknown}")
    private String system;



    @PostConstruct
    public void init() {
        configurationUtil.addMetric("target.info", 1,
                        "service.name", this.serviceName,
                        "environment",  this.environment,
                        "system",  this.system
                );
    }

    /**
     * Redirects requests from /metrics to /actuator/prometheus.
     * This ensures compatibility with legacy systems expecting /metrics.
     */
    @Bean
    public RouterFunction<ServerResponse> redirectMetrics() {
        return route(GET("/metrics"),
                req -> ServerResponse.permanentRedirect(URI.create("/actuator/prometheus")).build()
        );
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
}