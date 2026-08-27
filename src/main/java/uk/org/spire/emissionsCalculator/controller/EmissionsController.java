package uk.org.spire.emissionsCalculator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.spire.emissionsCalculator.dto.EmissionRequest;
import uk.org.spire.emissionsCalculator.dto.EmissionResponse;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;
import uk.org.spire.emissionsCalculator.service.EmissionCalculationService;

/**
 * REST API Controller for managing and processing fuel emission data.
 * <p>
 * Exposes endpoints for calculating environmental impacts based on the CETESB methodology.
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
     * Calculates the estimated VOC emissions based on the provided fuel details.
     *
     * @param request The {@link EmissionRequest} containing fuel type, volume, and temperature.
     * @return A {@link ResponseEntity} containing the {@link EmissionResponse} and HTTP 201 Created.
     */
    @PostMapping("/calculate")
    public ResponseEntity<EmissionResponse> calculateEmissions(@RequestBody EmissionRequest request) {

        PetrolStationEmission savedRecord = calculationService.calculateAndSaveEmissions(
                request.fuelType(),
                request.fuelVolumeLitres(),
                request.ambientTemperatureCelsius()
        );

        EmissionResponse response = new EmissionResponse(savedRecord.getCalculatedVocEmissionsKg());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}