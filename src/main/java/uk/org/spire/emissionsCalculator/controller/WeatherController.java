package uk.org.spire.emissionsCalculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentWeather(
            @RequestParam double lat,
            @RequestParam double lon) {

        // Retorna um Mock de clima/vento inicial para a interface responder com sucesso (200 OK)
        Map<String, Object> weatherData = Map.of(
                "temperature", 17.2,
                "windSpeed", 2.0,
                "windDirection", 180,
                "humidity", 65,
                "latitude", lat,
                "longitude", lon
        );

        return ResponseEntity.ok(weatherData);
    }
}