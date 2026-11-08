package com.example.app;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static org.assertj.core.api.Assertions.assertThat;

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
class LoggingTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private ByteArrayOutputStream out;

    @Autowired
    private TestService testService;

    @BeforeEach
    void before() {
        out = new ByteArrayOutputStream();

        Appender<ILoggingEvent> console = logger.getAppender("STDOUT");
        assertThat(console).isInstanceOf(ConsoleAppender.class);

        Encoder<ILoggingEvent> encoder = ((ConsoleAppender<ILoggingEvent>) console).getEncoder();

        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setContext(logger.getLoggerContext());
        appender.setOutputStream(out);
        appender.setEncoder(encoder);
        appender.start();

        logger.addAppender(appender);
    }

    @Test
    void logsInJsonFormatWithCorrectFields() {
        MDC.put("traceId", "aaaa");
        MDC.put("spanId", "bbbb");

        logger.info("hello", kv("asdasd", "asdasd"));

        String jsonLine = out.toString(StandardCharsets.UTF_8);
        JsonNode json = JsonMapper.shared().readTree(jsonLine);

        assertThat(json.get("timestamp").asString()).isNotEmpty();
        assertThat(json.get("level").asString()).isEqualTo("INFO");
        assertThat(json.get("attributes").get("system").asString()).isEqualTo("test-system");
        assertThat(json.get("attributes").get("service.name").asString()).isEqualTo("test-service");
        assertThat(json.get("attributes").get("environment").asString()).isEqualTo("test-env");
        assertThat(json.get("message").asString()).isEqualTo("hello");
        assertThat(json.get("asdasd").asString()).isEqualTo("asdasd");
        assertThat(json.get("spanContext").get("traceId").asString()).isEqualTo("aaaa");
        assertThat(json.get("spanContext").get("spanId").asString()).isEqualTo("bbbb");
    }

    @Test
    void checkTimestampFormat() {
        logger.info("hello");

        String jsonLine = out.toString(StandardCharsets.UTF_8);
        JsonNode json = JsonMapper.shared().readTree(jsonLine);


        String timestamp = json.get("timestamp").asString();
        ZonedDateTime z =  ZonedDateTime.parse(timestamp);

        assertThat(timestamp).endsWith("Z");
        assertThat(z).isNotNull();
    }

    @Test
    void logsWithSpanAnnotation() {
        testService.spanFunc("log in span", logger);

        String jsonLine = out.toString(StandardCharsets.UTF_8);
        JsonNode json = JsonMapper.shared().readTree(jsonLine);

        assertThat(json.get("message").asString()).isEqualTo("log in span");

        assertThat(json.get("spanContext").get("traceId").asString()).isNotNull();
        assertThat(json.get("spanContext").get("spanId").asString()).isNotNull();
    }

}