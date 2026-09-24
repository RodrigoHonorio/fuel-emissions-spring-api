package uk.org.spire.emissionsCalculator.controller;

import org.springframework.web.bind.annotation.*;
import uk.org.spire.emissionsCalculator.dto.VocHeatmapDTO;
import uk.org.spire.emissionsCalculator.service.EmissionImpactService;

@RestController
@RequestMapping("/api/v1/emissions")
@CrossOrigin(origins = "*")
public class EmissionImpactController {

    private final EmissionImpactService emissionImpactService;

    public EmissionImpactController(EmissionImpactService emissionImpactService) {
        this.emissionImpactService = emissionImpactService;
    }

    @GetMapping("/voc-heatmap")
    public VocHeatmapDTO getVocHeatmap(
            @RequestParam(defaultValue = "20.0") double temp,
            @RequestParam(defaultValue = "60.0") double humidity,
            @RequestParam(defaultValue = "1013.0") double pressure,
            @RequestParam(defaultValue = "400.0") double solarRad,
            @RequestParam(defaultValue = "3.5") double windSpeed,
            @RequestParam(defaultValue = "240.0") double windDirection,
            @RequestParam(defaultValue = "both") String fuelType
    ) {
        return emissionImpactService.calculateCityWideVocPlumesAndHeatmap(temp, humidity, pressure, solarRad, windSpeed, windDirection, fuelType);
    }
}