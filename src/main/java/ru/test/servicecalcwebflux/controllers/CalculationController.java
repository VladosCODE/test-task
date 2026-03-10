package ru.test.servicecalcwebflux.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.test.servicecalcwebflux.service.CalculationService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class CalculationController {
    private final CalculationService service;

    @GetMapping(value = "/calculate", produces = "text/event-stream")
    public Flux<String> calculate(
            @RequestParam int count,
            @RequestParam boolean ordered) {
        return service.calculate(count, ordered);
    }
}
