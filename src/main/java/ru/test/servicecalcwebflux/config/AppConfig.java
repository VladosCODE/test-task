package ru.test.servicecalcwebflux.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Bean для хранения функций(скриптов) и кол-ва итераций
 */
@Component
@Getter
@Setter
public class AppConfig {
    private String function1; //Функция 1
    private String function2; //Функция 2
    private int interval; //Интервал между итерациями
}
