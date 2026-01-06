package com.example.app;

import ch.qos.logback.classic.Logger;
import com.example.app.annotation.Span;
import org.springframework.stereotype.Service;


@Service
public class TestService {
    @Span(name="my_span")
    void spanFunc(String message, Logger logger) {
        logger.info(message);
    }
}
