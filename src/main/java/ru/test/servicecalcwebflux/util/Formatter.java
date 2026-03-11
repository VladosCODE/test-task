package ru.test.servicecalcwebflux.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.test.servicecalcwebflux.model.Result;

@Component
@NoArgsConstructor
public class Formatter {

    public String formatUnordered(Result r) {
        if (r.isError()) {
            return String.format("%d,%d,error: %s", r.getIteration(), r.getFunction(), r.getError());
        } else {
            return String.format("%d,%d,%s,%d", r.getIteration(), r.getFunction(), r.getValue(), r.getTimestamp());
        }
    }

    public String formatOrdered(Result r1, Result r2, int buf1, int buf2) {
        return String.format("%d,%s,%d,%d,%s,%d,%d",
                r1.getIteration(),
                r1.isError() ? "error: " + r1.getError() : String.valueOf(r1.getValue()),
                r1.getTimestamp(),
                buf1,
                r2.isError() ? "error: " + r2.getError() : String.valueOf(r2.getValue()),
                r2.getTimestamp(),
                buf2);
    }
}
