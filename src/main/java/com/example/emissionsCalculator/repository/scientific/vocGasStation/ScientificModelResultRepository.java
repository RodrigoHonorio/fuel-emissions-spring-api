package com.example.emissionsCalculator.repository.scientific.vocGasStation;

import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScientificModelResultRepository extends JpaRepository<ScientificModelResult, Long> {

    List<ScientificModelResult> findByModelNameOrderByCalculationTimestampDesc(String modelName);

    List<ScientificModelResult> findByGasStationIdOrderByCalculationTimestampDesc(Long gasStationId);
}
