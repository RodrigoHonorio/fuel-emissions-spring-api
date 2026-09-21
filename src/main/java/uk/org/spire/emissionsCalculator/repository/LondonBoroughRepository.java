package uk.org.spire.emissionsCalculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.org.spire.emissionsCalculator.model.LondonBorough;

import java.util.Optional;

@Repository
public interface LondonBoroughRepository extends JpaRepository<LondonBorough, Long> {
    Optional<LondonBorough> findByName(String name);
}