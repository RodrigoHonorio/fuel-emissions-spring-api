package uk.org.spire.emissionsCalculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.org.spire.emissionsCalculator.dto.SpatialNodeResponse;
import uk.org.spire.emissionsCalculator.repository.SpatialNodeRepository;

import java.util.List;

/**
 * REST Controller responsible for managing and exposing spatial nodes
 * (monitoring stations and geographical coordinates) to the S.P.I.R.E. dashboard.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class SpatialNodeController {

    private final SpatialNodeRepository repository;

    /**
     * Constructs the Spatial Node Controller.
     *
     * @param repository The data access object for spatial nodes.
     */
    public SpatialNodeController(SpatialNodeRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves all registered spatial nodes.
     * Mapped to support all route variations requested by the frontend.
     *
     * @return A list of {@link SpatialNodeResponse} containing geographical coordinates and AQI status.
     */
    @GetMapping({"/api/v1/spatial-nodes", "/spatial-nodes", "/api/v1/spatial/nodes"})
    public ResponseEntity<List<SpatialNodeResponse>> getAllNodes() {
        List<SpatialNodeResponse> responseList = repository.findAll().stream()
                .map(node -> new SpatialNodeResponse(
                        node.getId(),
                        node.getStationName(),
                        node.getCoordinates().getY(), // Y maps to Latitude
                        node.getCoordinates().getX(), // X maps to Longitude
                        node.getAqiStatus()           // AQI status for color-coding on map
                ))
                .toList();

        return ResponseEntity.ok(responseList);
    }
}