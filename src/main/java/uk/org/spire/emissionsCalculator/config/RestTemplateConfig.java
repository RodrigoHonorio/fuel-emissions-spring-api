package uk.org.spire.emissionsCalculator.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10)) // Timeout de conexão (10 segundos)
                .setReadTimeout(Duration.ofSeconds(60))    // Timeout de leitura estipulado (60 segundos)
                .build();
    }
}