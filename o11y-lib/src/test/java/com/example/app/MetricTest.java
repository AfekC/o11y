package com.example.app;

import com.example.app.util.ConfigurationUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {
                TestApplication.class,
                TestConfig.class
        },
        properties = {
                "environment=test-env",
                "service.name=test-service",
                "system=test-system"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ImportAutoConfiguration(ObservabilityAutoConfiguration.class)
class MetricTest {

    @Autowired
    private ConfigurationUtil configurationUtil;

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private ApplicationContext context;

    @Test
    void observabilityAutoConfigurationWasLoaded() {
        assertTrue(
                context.containsBean("observabilityAutoConfiguration"),
                "ObservabilityAutoConfiguration should be loaded"
        );
    }

    @Test
    void shouldExposeTargetInfoMetric() {
        var gauge = registry.find("target.info").gauge();
        assertNotNull(gauge, "target.info should be registered");
        assertEquals("test-env",
                gauge.getId().getTag("environment"));
    }

    @Test
    void exposeCustomMetric() {
        configurationUtil.addMetric("my.custom.metric", 1, "key1", "value1");

        var gauge = registry.find("my.custom.metric").gauge();
        assertNotNull(gauge, "my.custom.metric should be registered");
        assertEquals("value1",
                gauge.getId().getTag("key1"));
    }

    @Test
    void getTargetInfoMetric() {
        RequiredSearch requiredSearch = configurationUtil.getMetric("target.info");

        var gauge = requiredSearch.gauge();
        assertNotNull(gauge, "target.info should be registered");
        assertEquals("test-env",
                gauge.getId().getTag("environment"));
    }
}
