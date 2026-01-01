package com.example.app.config;

import com.example.app.util.ConfigurationUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigExample {

    @Autowired
    private ConfigurationUtil configurationUtil;

    @PostConstruct
    private void init() {
        configurationUtil.addMetric("my.test.metric", 0, "key1", "value1", "key2", "value2");
    }

    @Bean
    Counter testCounterTotal(MeterRegistry registry) {
        Counter counter = Counter.builder("test_counter_total").register(registry);
        counter.increment();
        return counter;
    }
}
