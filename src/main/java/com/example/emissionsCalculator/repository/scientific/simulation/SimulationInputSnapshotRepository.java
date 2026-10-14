package com.example.emissionsCalculator.repository.scientific.simulation;

import com.example.emissionsCalculator.entity.scientific.simulation.SimulationInputSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulationInputSnapshotRepository extends JpaRepository<SimulationInputSnapshot, Long> {
    // O JpaRepository já disponibiliza automaticamente o saveAll() para esta entidade
}