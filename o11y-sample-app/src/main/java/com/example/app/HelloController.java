package com.example.app;

import com.example.app.annotation.Span;
import io.micrometer.core.instrument.Counter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private UtilExample utilExample;

    @Autowired
    private Counter testCounterTotal;


    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @PostConstruct
    public void func() {
        logger.info(utilExample.func());
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        logger.info("Handling hello request for: {}", name);

        //increment Bean counter
        testCounterTotal.increment();

        // Run a function with span
        utilExample.spanInSpan();

        //Span cannot be trigger with call from the same class(scope)
        spanInTheSameClass();

        logger.info("Finish handling hello request");

        return "Hello, " + name + "!";
    }

    @Span(name = "will_not_work")
    public void spanInTheSameClass() {
        logger.info("To make @Span work you must call the function from different bean");
    }


}
