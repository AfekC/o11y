package com.example.app;

import com.example.app.aspect.SpanAspect;
import com.example.app.util.ConfigurationUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumers live in their own packages and are often not web apps: the library must work from
 * its auto-configuration alone (no component scan of com.example.app, no hand-registered beans).
 */
class AutoConfigurationTest {

    private static final AutoConfigurations O11Y = AutoConfigurations.of(ObservabilityAutoConfiguration.class);

    @Test
    void nonWebAppWithoutSpringMvc_getsCoreBeans_andNoMetricsRedirect() {
        new ApplicationContextRunner()
                .withConfiguration(O11Y)
                .withClassLoader(new FilteredClassLoader("org.springframework.web.servlet"))
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .withBean(ObservationRegistry.class, ObservationRegistry::create)
                .withPropertyValues("environment=test-env")
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).hasSingleBean(ConfigurationUtil.class);
                    assertThat(ctx).hasSingleBean(SpanAspect.class);
                    assertThat(ctx).doesNotHaveBean("redirectMetrics");
                    assertThat(ctx.getBean(MeterRegistry.class).find("target.info").gauge())
                            .isNotNull()
                            .satisfies(g -> assertThat(g.getId().getTag("environment")).isEqualTo("test-env"));
                });
    }

    @Test
    void servletWebApp_getsMetricsRedirect() {
        new WebApplicationContextRunner()
                .withConfiguration(O11Y)
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .withBean(ObservationRegistry.class, ObservationRegistry::create)
                .run(ctx -> assertThat(ctx).hasBean("redirectMetrics"));
    }

    @Test
    void userDefinedConfigurationUtil_winsOverTheDefault() {
        ConfigurationUtil mine = new ConfigurationUtil();
        new ApplicationContextRunner()
                .withConfiguration(O11Y)
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .withBean(ObservationRegistry.class, ObservationRegistry::create)
                .withBean(ConfigurationUtil.class, () -> mine)
                .run(ctx -> assertThat(ctx.getBean(ConfigurationUtil.class)).isSameAs(mine));
    }
}
