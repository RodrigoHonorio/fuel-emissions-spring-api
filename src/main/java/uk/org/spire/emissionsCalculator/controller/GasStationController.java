package uk.org.spire.emissionsCalculator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.org.spire.emissionsCalculator.dto.GasStationDTO;
import uk.org.spire.emissionsCalculator.service.GasStationService;
import uk.org.spire.emissionsCalculator.service.WeatherService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gas-stations")
public class GasStationController {

    private final GasStationService gasStationService;
    private final WeatherService weatherService;

    public GasStationController(GasStationService gasStationService, WeatherService weatherService) {
        this.gasStationService = gasStationService;
        this.weatherService = weatherService;
    }

    @GetMapping
    public List<GasStationDTO> getGasStations(
            @RequestParam(required = false) Double temp,
            @RequestParam(defaultValue = "10000") double volume,
            @RequestParam(required = false) Double humidity,
            @RequestParam(required = false) Double pressure,
            @RequestParam(required = false) Double solar,
            @RequestParam(required = false) Double wind,
            @RequestParam(defaultValue = "both") String fuelType) {

        // Valores padrão seguros caso o serviço de clima externo falhe
        double t = 15.0, h = 70.0, p = 1013.25, s = 0.0, w = 3.5;

        try {
            WeatherService.WeatherData liveWeather = weatherService.fetchCurrentWeatherForLondon();
            if (liveWeather != null) {
                t = (temp != null) ? temp : liveWeather.temp;
                h = (humidity != null) ? humidity : liveWeather.humidity;
                p = (pressure != null) ? pressure : liveWeather.pressure;
                s = (solar != null) ? solar : liveWeather.solarRadiation;
                w = (wind != null) ? wind : liveWeather.windSpeed;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Falha ao obter clima em tempo real. A usar valores padrão: " + e.getMessage());
            if (temp != null) t = temp;
            if (humidity != null) h = humidity;
            if (pressure != null) p = pressure;
            if (solar != null) s = solar;
            if (wind != null) w = wind;
        }

        return gasStationService.getAllLondonGasStations(t, volume, h, p, s, w, fuelType);
    }
}