package ru.test.servicecalcwebflux.evaluator;

import reactor.core.publisher.Mono;
import ru.test.servicecalcwebflux.dto.ResultDto;

public interface ScriptEvaluator {
    /**
     * Запуск пользовательских функций
     * @param script пользовательская функция
     * @param argument аргумент
     * @return значение пользовательской функции
     */
    Mono<ResultDto> evaluate(String script, int argument);
}
