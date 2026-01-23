package br.com.postech.feedback.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JsonConfig {

    @Bean
    @Primary // Este será o ObjectMapper padrão para todas as Lambdas
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Suporte para datas do Java 8+ (LocalDateTime)
        mapper.registerModule(new JavaTimeModule());
        // Escrever datas como Strings ISO-8601 e não timestamps numéricos
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Adicionando formatação bonita do JSON
        return mapper;
    }
}