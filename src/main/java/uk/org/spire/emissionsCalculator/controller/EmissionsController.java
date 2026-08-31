package uk.org.spire.emissionsCalculator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.spire.emissionsCalculator.dto.EmissionDetailsResponse;
import uk.org.spire.emissionsCalculator.dto.EmissionRequest;
import uk.org.spire.emissionsCalculator.dto.EmissionResponse;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;
import uk.org.spire.emissionsCalculator.service.EmissionCalculationService;

import java.util.List;

/**
 * REST API Controller for managing and processing fuel emission data and spatial queries.
 * <p>
 * Exposes endpoints for calculating environmental impacts based on the CETESB methodology,
 * atmospheric dispersion models, and PostGIS spatial proximity searches.
 * </p>
 */
@RestController
@RequestMapping("/api/emissions")
@CrossOrigin(origins = "*")
public class EmissionsController {

    private final EmissionCalculationService calculationService;

    @Autowired
    public EmissionsController(EmissionCalculationService calculationService) {
        this.calculationService = calculationService;
    }

    /**
     * Calculates the estimated VOC emissions and atmospheric dispersion based on the provided fuel, wind, and location details.
     *
     * @param request The {@link EmissionRequest} containing fuel type, volume, temperature, wind data, and coordinates.
     * @return A {@link ResponseEntity} containing the {@link EmissionResponse} and HTTP 201 Created.
     */
    @PostMapping("/calculate")
    public ResponseEntity<EmissionResponse> calculateEmissions(@RequestBody EmissionRequest request) {

        PetrolStationEmission savedRecord = calculationService.calculateAndSaveEmissions(
                request.fuelType(),
                request.fuelVolumeLitres(),
                request.ambientTemperatureCelsius(),
                request.windSpeed(),
                request.windDirection(),
                request.latitude(),
                request.longitude()
        );

        EmissionResponse response = new EmissionResponse(savedRecord.getCalculatedVocEmissionsKg());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves emission records within a given geographic radius from a reference point using PostGIS.
     *
     * @param latitude  Latitude of the center point (e.g., central London).
     * @param longitude Longitude of the center point.
     * @param distance  Radius range in meters.
     * @return A {@link ResponseEntity} containing a list of matching {@link EmissionDetailsResponse} records.
     */
    @GetMapping("/radius")
    public ResponseEntity<List<EmissionDetailsResponse>> getEmissionsWithinRadius(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double distance) {

        List<PetrolStationEmission> results = calculationService.getEmissionsWithinRadius(latitude, longitude, distance);

        // Converts entities to DTOs to avoid JTS serialization recursion issues with Jackson
        List<EmissionDetailsResponse> responseList = results.stream()
                .map(EmissionDetailsResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responseList);
    }
}