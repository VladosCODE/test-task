package ru.test.servicecalcwebflux.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import ru.test.servicecalcwebflux.model.Result;
import ru.test.servicecalcwebflux.util.Formatter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderedMerger {
    private final ConcurrentMap<Integer, Result> map1 = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Result> map2 = new ConcurrentHashMap<>();
    private final Sinks.Many<String> outputSink = Sinks.many().unicast().onBackpressureBuffer();
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final Formatter formatter;

    public OrderedMerger(Formatter formatter) {
        this.formatter = formatter;
    }

    public void merge(Flux<Result> flux1, Flux<Result> flux2) {
        flux1.subscribe(
                this::processResult1,
                this::handleError,
                this::complete
        );
        flux2.subscribe(
                this::processResult2,
                this::handleError,
                this::complete
        );
    }

    private void processResult1(Result r) {
        map1.put(r.getIteration(), r);
        tryEmit(r.getIteration());
    }

    private void processResult2(Result r) {
        map2.put(r.getIteration(), r);
        tryEmit(r.getIteration());
    }

    private void tryEmit(int iteration) {
        Result r1 = map1.get(iteration);
        Result r2 = map2.get(iteration);
        if (r1 != null && r2 != null) {
            map1.remove(iteration);
            map2.remove(iteration);
            String line = formatter.formatOrdered(r1, r2, map1.size(), map2.size());
            outputSink.tryEmitNext(line);
        }
    }

    /*private String formatOrdered(Result r1, Result r2, int buf1, int buf2) {
        return String.format("%d,%s,%d,%d,%s,%d,%d",
                r1.getIteration(),
                r1.isError() ? "error: " + r1.getError() : String.valueOf(r1.getValue()),
                r1.getTimestamp(),
                buf1,
                r2.isError() ? "error: " + r2.getError() : String.valueOf(r2.getValue()),
                r2.getTimestamp(),
                buf2);
    }*/

    private void handleError(Throwable error) {
        outputSink.tryEmitError(error);
    }

    private void complete() {
        if (completedCount.incrementAndGet() == 2) {
            // Оба потока завершены, можно закрыть sink
            outputSink.tryEmitComplete();
        }
    }

    public Flux<String> getOutputFlux() {
        return outputSink.asFlux();
    }
}
