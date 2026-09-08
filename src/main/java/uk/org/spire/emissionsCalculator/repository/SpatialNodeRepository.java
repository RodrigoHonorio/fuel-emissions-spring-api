package uk.org.spire.emissionsCalculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.org.spire.emissionsCalculator.model.SpatialNode;

import java.util.Optional;

/**
 * Repository interface for managing {@link SpatialNode} entities.
 * <p>
 * Handles standard CRUD operations and spatial queries via Hibernate Spatial.
 * </p>
 */
@Repository
public interface SpatialNodeRepository extends JpaRepository<SpatialNode, Long> {

    /**
     * Checks whether a spatial node with the specified station name already exists in the database.
     *
     * @param stationName The unique name of the monitoring station.
     * @return {@code true} if a node with the station name exists, {@code false} otherwise.
     */
    boolean existsByStationName(String stationName);

    /**
     * Retrieves a spatial node by its station name.
     *
     * @param stationName The unique name of the monitoring station.
     * @return An {@link Optional} containing the found node, or empty if none exists.
     */
    Optional<SpatialNode> findByStationName(String stationName);
}