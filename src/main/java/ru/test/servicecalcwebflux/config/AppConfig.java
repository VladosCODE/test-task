package ru.test.servicecalcwebflux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public ConfigFile configFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("config.json");
        return objectMapper().readValue(resource.getInputStream(), ConfigFile.class);
    }
}
