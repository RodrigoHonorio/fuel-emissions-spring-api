package com.example.emissionsCalculator.repository.scientific.simulation;

import com.example.emissionsCalculator.entity.scientific.simulation.SimulationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulationBatchRepository extends JpaRepository<SimulationBatch, Long> {
}
