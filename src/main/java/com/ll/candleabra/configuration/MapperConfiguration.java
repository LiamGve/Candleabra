package com.ll.candleabra.configuration;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class MapperConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();

        final SimpleModule module = new SimpleModule();
        objectMapper.registerModule(new JavaTimeModule());
        module.addKeyDeserializer(LocalDateTime.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                final LocalDate ld = LocalDate.parse(key, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return LocalDateTime.of(ld, LocalDateTime.now().toLocalTime());
            }
        });
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
