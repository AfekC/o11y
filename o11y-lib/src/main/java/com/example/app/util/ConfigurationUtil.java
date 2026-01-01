package com.example.app.util;

import io.micrometer.common.lang.Nullable;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.search.RequiredSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationUtil {

    @Autowired
    private MeterRegistry meterRegistry;

    public void addMetric(String metricName, Number n, @Nullable String... keyValues) {
        meterRegistry.gauge(metricName,
                Tags.of(keyValues),
                n);
    }

    public RequiredSearch getMetric(String metricName) {
        return this.meterRegistry.get(metricName);
    }
}
