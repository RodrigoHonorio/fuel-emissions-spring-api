package uk.org.spire.emissionsCalculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.org.spire.emissionsCalculator.dto.LondonAirQualityResponse;
import uk.org.spire.emissionsCalculator.integration.LondonAirQualityClient;

@RestController
@RequestMapping("/api/v1/london/air-quality")
public class LondonDataController {

    private final LondonAirQualityClient airQualityClient;

    public LondonDataController(LondonAirQualityClient airQualityClient) {
        this.airQualityClient = airQualityClient;
    }

    @GetMapping
    public ResponseEntity<LondonAirQualityResponse> getLondonPollutionData() {
        LondonAirQualityResponse data = airQualityClient.fetchLondonAirQuality();
        return ResponseEntity.ok(data);
    }
}