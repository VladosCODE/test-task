package ru.test.servicecalcwebflux.model;

import lombok.Getter;

@Getter
public class Result {
    private final int iteration; // номер итерации
    private final int function; // номер функции
    private final Double value; // значение функции
    private final String error; // формулировка ошибки
    private final long timestamp; // время вычисления

    private Result(int iteration, int function, Double value, String error, long timestamp) {
        this.iteration = iteration;
        this.function = function;
        this.value = value;
        this.error = error;
        this.timestamp = timestamp;
    }

    public static Result success(int iteration, int function, double value, long timestamp) {
        return new Result(iteration, function, value, null, timestamp);
    }

    public static Result error(int iteration, int function, String error, long timestamp) {
        return new Result(iteration, function, null, error, timestamp);
    }

    public boolean isError() { return error != null; }
}
