package ru.test.servicecalcwebflux.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.test.servicecalcwebflux.config.ConfigFile;
import ru.test.servicecalcwebflux.dto.ResultDto;
import ru.test.servicecalcwebflux.evaluator.ScriptEvaluator;
import ru.test.servicecalcwebflux.model.Result;
import ru.test.servicecalcwebflux.util.Formatter;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CalculationService {
    private final ConfigFile config;
    private final ScriptEvaluator evaluator;
    private final Formatter formatter;

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
                .map(res -> Result.success((int) iteration, 1, res.getValue(), res.getTimeCalc()))
                .onErrorResume(e -> Mono.just(Result.error((int) iteration, 1, e.getMessage())));

        Mono<Result> r2 = evaluate(iteration, 2)
                .map(r -> Result.success((int) iteration, 2, r.getValue(),r.getTimeCalc()))
                .onErrorResume(e -> Mono.just(Result.error((int) iteration, 2, e.getMessage())));

        return Flux.merge(r1, r2).map(formatter::formatUnordered);
    }

    private Flux<String> processOrdered(Flux<Long> ticks) {
        OrderedMerger merger = new OrderedMerger(formatter);

        Flux<Result> allResults = ticks.flatMap(iteration ->
                Flux.merge(
                        evaluate(iteration, 1)
                                .map(res -> Result.success(iteration.intValue(), 1, res.getValue(), res.getTimeCalc()))
                                .onErrorResume(e -> Mono.just(Result.error(iteration.intValue(), 1, e.getMessage()))),
                        evaluate(iteration, 2)
                                .map(res -> Result.success(iteration.intValue(), 2, res.getValue(), res.getTimeCalc()))
                                .onErrorResume(e -> Mono.just(Result.error(iteration.intValue(), 2, e.getMessage())))
                )
        );

        Flux<Result> results1 = allResults.filter(r -> r.getFunction() == 1);
        Flux<Result> results2 = allResults.filter(r -> r.getFunction() == 2);

        merger.merge(results1, results2);
        return merger.getOutputFlux();
    }

    private Mono<ResultDto> evaluate(long iteration, int function) {
        String script = function == 1 ? config.getFunction1() : config.getFunction2();
        return evaluator.evaluate(script, (int) iteration);
    }
}
