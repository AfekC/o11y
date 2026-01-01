package com.example.app;

import com.example.app.annotation.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;


@Component
public class UtilExample {
    private static final Logger logger = LoggerFactory.getLogger(UtilExample.class);

    @Span(name = "spanWithLogAndReturn")
    public String func() {
        logger.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", kv("aaaaaaaaaaaaa", "dasasdasdasd"));
        return "Afek :)";
    }

    @Span(name = "span_in_span")
    public void spanInSpan() {
        logger.info("Starting complex work...", kv("aaa", io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceFlags()));

        io.opentelemetry.api.trace.Span.current().addEvent("complex_work_finished");
        io.opentelemetry.api.trace.Span.current().setAttribute("nice_attribute", 7);
    }
}
