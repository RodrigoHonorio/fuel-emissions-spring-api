package com.example.emissionsCalculator.controller.weather;

import com.example.emissionsCalculator.service.weather.WeatherIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>O que é:</b> Controlador REST para sincronização de dados meteorológicos.
 * <b>Para que serve:</b> Expor o endpoint POST para carregar e atualizar a série temporal de clima.
 * <b>De onde veio:</b> Spring MVC / Swagger UI.
 */
@RestController
@RequestMapping("/api/weather-forecast")
@RequiredArgsConstructor
public class WeatherIngestionController {

    private final WeatherIngestionService service;

    /**
     * <b>O que é:</b> Endpoint de disparar a sincronização do clima.
     * <b>Para que serve:</b> Baixar as 384 horas futuras de temperatura e acumular o histórico.
     *
     * @return ResponseEntity com mensagem do resultado da operação.
     */
    @PostMapping("/sync")
    public ResponseEntity<String> syncWeather() {
        try {
            int count = service.syncWeatherForecast();
            return ResponseEntity.ok("Sincronização meteorológica concluída com sucesso! " + count + " registos de horas atualizados/salvos.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao sincronizar dados do clima: " + e.getMessage());
        }
    }
}