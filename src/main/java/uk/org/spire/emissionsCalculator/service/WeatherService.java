package uk.org.spire.emissionsCalculator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherData fetchCurrentWeatherForLondon() {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=51.5074&longitude=-0.1278&current=temperature_2m,relative_humidity_2m,surface_pressure,wind_speed_10m,wind_direction_10m,direct_radiation";

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode current = root.path("current");

            double temp = current.path("temperature_2m").asDouble(17.2);
            double humidity = current.path("relative_humidity_2m").asDouble(65.0);
            double pressure = current.path("surface_pressure").asDouble(1014.0);
            double windSpeed = current.path("wind_speed_10m").asDouble(3.2);
            double windDirection = current.path("wind_direction_10m").asDouble(180.0);
            double solarRadiation = current.path("direct_radiation").asDouble(450.0);

            return new WeatherData(temp, humidity, pressure, windSpeed, windDirection, solarRadiation);
        } catch (Exception e) {
            System.err.println("Erro ao buscar clima da API Open-Meteo, usando valores padrão: " + e.getMessage());
            return new WeatherData(17.2, 65.0, 1014.0, 3.2, 180.0, 450.0);
        }
    }

    public static class WeatherData {
        public double temp;
        public double humidity;
        public double pressure;
        public double windSpeed;
        public double windDirection;
        public double solarRadiation;

        public WeatherData(double temp, double humidity, double pressure, double windSpeed, double windDirection, double solarRadiation) {
            this.temp = temp;
            this.humidity = humidity;
            this.pressure = pressure;
            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
            this.solarRadiation = solarRadiation;
        }
    }
}