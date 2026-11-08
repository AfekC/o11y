package com.example.app;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Adds the library's defaults ({@code o11y-defaults.yaml}) as the lowest-precedence property source,
 * so they apply in every consuming app while its own config and env vars still override them.
 */
public class O11yDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String NAME = "o11y-defaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(NAME)) {
            return;
        }
        try {
            for (PropertySource<?> source : new YamlPropertySourceLoader()
                    .load(NAME, new ClassPathResource(NAME + ".yaml"))) {
                environment.getPropertySources().addLast(source);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + NAME + ".yaml", e);
        }
    }
}
