package ru.test.servicecalcwebflux.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.test.servicecalcwebflux.config.AppConfig;
import ru.test.servicecalcwebflux.evaluator.ScriptEvaluator;
import ru.test.servicecalcwebflux.model.Result;

import java.time.Duration;

@Service
public class CalculationService {
    private final AppConfig config;
    private final ScriptEvaluator evaluator;

    public CalculationService(AppConfig config, ScriptEvaluator evaluator) {
        this.config = config;
        this.evaluator = evaluator;
    }

    /**
     * Метод, который вычисляет функции js/python итерационно и передает в поток клиенту
     * @param count кол-во итераций
     * @param ordered флаг упорядоченности результатов
     * @return поток результатов вычислений функций js/python
     */
    public Flux<String> calculate(int count, boolean ordered) {
        Flux<Long> ticks = Flux.interval(Duration.ofMillis(config.getInterval()))
                .take(count)
                .map(i -> i + 1); // итерации с 1

        if (!ordered) {
            return ticks.flatMap(this::processIterationUnordered);
        } else {
            return processOrdered(ticks);
        }
    }

    private Flux<String> processIterationUnordered(long iteration) {
        Mono<Result> r1 = evaluate(iteration, 1)
                .map(v -> Result.success((int) iteration, 1, v, System.currentTimeMillis()))
                .onErrorResume(e -> Mono.just(Result.error((int) iteration, 1, e.getMessage(), System.currentTimeMillis())));

        Mono<Result> r2 = evaluate(iteration, 2)
                .map(v -> Result.success((int) iteration, 2, v, System.currentTimeMillis()))
                .onErrorResume(e -> Mono.just(Result.error((int) iteration, 2, e.getMessage(), System.currentTimeMillis())));

        return Flux.merge(r1, r2).map(this::formatUnordered);
    }

    private String formatUnordered(Result r) {
        if (r.isError()) {
            return String.format("%d,%d,error: %s", r.getIteration(), r.getFunction(), r.getError());
        } else {
            return String.format("%d,%d,%s,%d", r.getIteration(), r.getFunction(), r.getValue(), r.getTimestamp());
        }
    }

    private Flux<String> processOrdered(Flux<Long> ticks) {
        OrderedMerger merger = new OrderedMerger();

        Flux<Result> allResults = ticks.flatMap(iteration ->
                Flux.merge(
                        evaluate(iteration, 1)
                                .map(v -> Result.success(iteration.intValue(), 1, v, System.currentTimeMillis()))
                                .onErrorResume(e -> Mono.just(Result.error(iteration.intValue(), 1, e.getMessage(), System.currentTimeMillis()))),
                        evaluate(iteration, 2)
                                .map(v -> Result.success(iteration.intValue(), 2, v, System.currentTimeMillis()))
                                .onErrorResume(e -> Mono.just(Result.error(iteration.intValue(), 2, e.getMessage(), System.currentTimeMillis())))
                )
        );

        Flux<Result> results1 = allResults.filter(r -> r.getFunction() == 1);
        Flux<Result> results2 = allResults.filter(r -> r.getFunction() == 2);

        merger.merge(results1, results2);
        return merger.getOutputFlux();
    }

    private Mono<Double> evaluate(long iteration, int function) {
        String script = function == 1 ? config.getFunction1() : config.getFunction2();
        return evaluator.evaluate(script, (int) iteration);
    }
}
