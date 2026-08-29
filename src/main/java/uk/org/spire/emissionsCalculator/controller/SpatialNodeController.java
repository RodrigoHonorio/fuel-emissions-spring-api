package uk.org.spire.emissionsCalculator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.org.spire.emissionsCalculator.dto.SpatialNodeResponse;
import uk.org.spire.emissionsCalculator.repository.SpatialNodeRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller responsible for exposing spatial node endpoints to the frontend.
 */
@RestController
@RequestMapping("/api/v1/spatial")
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
     *
     * @return A list of {@link SpatialNodeResponse} containing geographical coordinates.
     */
    @GetMapping("/nodes")
    public ResponseEntity<List<SpatialNodeResponse>> getAllNodes() {
        List<SpatialNodeResponse> responseList = repository.findAll().stream()
                .map(node -> new SpatialNodeResponse(
                        node.getId(),
                        node.getStationName(),
                        node.getCoordinates().getY(), // Y maps to Latitude
                        node.getCoordinates().getX()  // X maps to Longitude
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
