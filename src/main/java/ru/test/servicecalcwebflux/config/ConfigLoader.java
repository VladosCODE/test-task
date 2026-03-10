package ru.test.servicecalcwebflux.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class ConfigLoader {
    private final AppConfig appConfig;
    private final ObjectMapper mapper;

    public ConfigLoader(AppConfig appConfig, ObjectMapper mapper) {
        this.appConfig = appConfig;
        this.mapper = mapper;
    }

    /**
     * Метод для инициализации bean с функциями из файла
     * @throws IOException
     */
    @PostConstruct
    public void loadConfig() throws IOException {
        ClassPathResource resource = new ClassPathResource("config.json");
        AppConfig loaded = mapper.readValue(resource.getInputStream(), AppConfig.class);
        appConfig.setFunction1(loaded.getFunction1());
        appConfig.setFunction2(loaded.getFunction2());
        appConfig.setInterval(loaded.getInterval());
    }
}
