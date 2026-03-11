package ru.test.servicecalcwebflux.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Для хранения функций(скриптов) и кол-ва итераций
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConfigFile {
    private String function1; //Функция 1
    private String function2; //Функция 2
    private int interval; //Интервал между итерациями
}
