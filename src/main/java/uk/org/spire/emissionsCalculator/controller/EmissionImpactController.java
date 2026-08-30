package uk.org.spire.emissionsCalculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.org.spire.emissionsCalculator.dto.EmissionImpactResponse;
import uk.org.spire.emissionsCalculator.service.EmissionImpactService;

@RestController
@RequestMapping("/api/v1/emissions")
public class EmissionImpactController {

    private final EmissionImpactService impactService;

    public EmissionImpactController(EmissionImpactService impactService) {
        this.impactService = impactService;
    }

    @PostMapping("/impact")
    public ResponseEntity<EmissionImpactResponse> calculateImpact(
            @RequestParam String fuelType,
            @RequestParam double volume,
            @RequestParam double latitude,
            @RequestParam double longitude) {

        EmissionImpactResponse response = impactService.calculateImpact(fuelType, volume, latitude, longitude);
        return ResponseEntity.ok(response);
    }
}