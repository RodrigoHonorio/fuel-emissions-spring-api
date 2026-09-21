package uk.org.spire.emissionsCalculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.org.spire.emissionsCalculator.model.GasStation;

import java.util.Optional;

@Repository
public interface GasStationRepository extends JpaRepository<GasStation, Long> {
    Optional<GasStation> findByOsmId(Long osmId);
}