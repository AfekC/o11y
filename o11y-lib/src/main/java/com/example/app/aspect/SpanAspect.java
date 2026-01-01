package com.example.app.aspect;

import com.example.app.annotation.Span;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class SpanAspect {

    @Autowired
    private ObservationRegistry registry;

    @Around("@annotation(span)")
    public Object handleLogExecutionTime(ProceedingJoinPoint joinPoint, Span span) {
        return Observation.createNotStarted(span.name(), this.registry)
            .observe(() -> {
                Map<String, String> oldMDC = MDC.getCopyOfContextMap();
                if (oldMDC == null) {
                    MDC.put("traceId", io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId());
                    MDC.put("spanId", io.opentelemetry.api.trace.Span.current().getSpanContext().getSpanId());
                }
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                } finally {
                    MDC.setContextMap(oldMDC);
                }
            });
    }
}
