package uk.org.spire.emissionsCalculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.org.spire.emissionsCalculator.dto.SpatialNodeResponse;
import uk.org.spire.emissionsCalculator.service.SpatialNodeService;

import java.util.List;

/**
 * REST Controller responsible for managing and exposing spatial nodes
 * (monitoring stations and geographical coordinates) to the S.P.I.R.E. dashboard.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class SpatialNodeController {

    private final SpatialNodeService spatialNodeService;

    /**
     * Constructs the Spatial Node Controller.
     *
     * @param spatialNodeService The service exposing spatial nodes ready for rendering.
     */
    public SpatialNodeController(SpatialNodeService spatialNodeService) {
        this.spatialNodeService = spatialNodeService;
    }

    /**
     * Retrieves the registered spatial nodes located within Greater London.
     * Mapped to support all route variations requested by the frontend.
     *
     * @return A list of {@link SpatialNodeResponse} with coordinates, AQI classification,
     * rendering colours and the current meteorological readings.
     */
    @GetMapping({"/api/v1/spatial-nodes", "/spatial-nodes", "/api/v1/spatial/nodes"})
    public ResponseEntity<List<SpatialNodeResponse>> getAllNodes() {
        return ResponseEntity.ok(spatialNodeService.findAllLondonNodes());
    }
}