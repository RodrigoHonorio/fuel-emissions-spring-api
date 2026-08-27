package uk.org.spire.emissionsCalculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;

/**
 * Repository interface for {@link PetrolStationEmission} instances.
 * <p>
 * Extends {@link JpaRepository} to provide standard CRUD operations
 * and database interactions for the emissions data.
 * </p>
 */
@Repository
public interface EmissionRepository extends JpaRepository<PetrolStationEmission, Long> {
}
