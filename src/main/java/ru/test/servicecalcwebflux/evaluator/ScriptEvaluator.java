package ru.test.servicecalcwebflux.evaluator;

import reactor.core.publisher.Mono;

public interface ScriptEvaluator {
    /**
     * Запуск пользовательских функций
     * @param script пользовательская функция
     * @param argument аргумент
     * @return значение пользовательской функции
     */
    Mono<Double> evaluate(String script, int argument);
}
