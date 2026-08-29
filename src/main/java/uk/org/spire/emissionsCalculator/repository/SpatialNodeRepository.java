package uk.org.spire.emissionsCalculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.org.spire.emissionsCalculator.model.SpatialNode;

/**
 * Repository interface for managing {@link SpatialNode} entities.
 * <p>
 * Handles standard CRUD operations and spatial queries via Hibernate Spatial.
 * </p>
 */
@Repository
public interface SpatialNodeRepository extends JpaRepository<SpatialNode, Long> {
}