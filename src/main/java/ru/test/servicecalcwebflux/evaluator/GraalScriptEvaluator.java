package ru.test.servicecalcwebflux.evaluator;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.test.servicecalcwebflux.dto.ResultDto;

@Component
public class GraalScriptEvaluator implements ScriptEvaluator {
    @org.springframework.beans.factory.annotation.Value("${script.language}")
    private String language;

    @Override
    public Mono<ResultDto> evaluate(String script, int argument) {
        return Mono.fromCallable(() -> {
            try (Context context = Context.create(language)) {
                long start = System.currentTimeMillis();
                // Делаем переменную x доступной внутри скрипта
                context.getBindings(language).putMember("x", argument);

                String scriptExecute = script;
                if(language.equals("js")){
                    scriptExecute = "(" + script + ")";
                }
                // Выполняем скрипт
                Value result = context.eval(language, scriptExecute);

                // Если скрипт вернул функцию, вызываем её с аргументом
                if (result.canExecute()) {
                    result = result.execute(argument);
                }

                long time = System.currentTimeMillis() - start;
                // Возвращаем число
                return new ResultDto(result.asDouble(),time);
            }
            catch (Exception e) {
                // Пробрасываем исключение, чтобы onErrorResume его обработал
                throw new RuntimeException("Script execution failed: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
