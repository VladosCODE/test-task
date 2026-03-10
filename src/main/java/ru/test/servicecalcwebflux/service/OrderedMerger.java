package ru.test.servicecalcwebflux.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import ru.test.servicecalcwebflux.model.Result;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrderedMerger {
    private final Queue<Result> queue1 = new ConcurrentLinkedQueue<>();
    private final Queue<Result> queue2 = new ConcurrentLinkedQueue<>();
    private final Sinks.Many<String> outputSink = Sinks.many().unicast().onBackpressureBuffer();
    private final AtomicBoolean completed1 = new AtomicBoolean(false);
    private final AtomicBoolean completed2 = new AtomicBoolean(false);

    public void merge(Flux<Result> flux1, Flux<Result> flux2) {
        flux1.subscribe(
                this::addResult1,
                error -> {},
                () -> {
                    completed1.set(true);
                    checkComplete();
                }
        );
        flux2.subscribe(
                this::addResult2,
                error -> {},
                () -> {
                    completed2.set(true);
                    checkComplete();
                }
        );
    }

    private synchronized void addResult1(Result r) {
        queue1.offer(r);
        tryEmit();
    }

    private synchronized void addResult2(Result r) {
        queue2.offer(r);
        tryEmit();
    }

    private void tryEmit() {
        while (true) {
            Result r1 = queue1.peek();
            Result r2 = queue2.peek();
            if (r1 == null || r2 == null) break;
            if (r1.getIteration() == r2.getIteration()) {
                queue1.poll();
                queue2.poll();
                String line = formatOrdered(r1, r2, queue1.size(), queue2.size());
                outputSink.tryEmitNext(line);
            } else {
                break; // ждём недостающий результат
            }
        }
    }

    private String formatOrdered(Result r1, Result r2, int buf1, int buf2) {
        return String.format("%d,%s,%d,%d,%s,%d,%d",
                r1.getIteration(),
                r1.isError() ? "error: " + r1.getError() : String.valueOf(r1.getValue()),
                r1.getTimestamp(),
                buf1,
                r2.isError() ? "error: " + r2.getError() : String.valueOf(r2.getValue()),
                r2.getTimestamp(),
                buf2);
    }

    private void checkComplete() {
        if (completed1.get() && completed2.get()) {
            outputSink.tryEmitComplete();
        }
    }

    public Flux<String> getOutputFlux() {
        return outputSink.asFlux();
    }
}
