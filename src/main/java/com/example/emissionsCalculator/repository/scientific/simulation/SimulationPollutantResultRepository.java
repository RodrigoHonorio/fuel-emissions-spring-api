package com.example.emissionsCalculator.repository.scientific.simulation;

import com.example.emissionsCalculator.entity.scientific.simulation.SimulationPollutantResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationPollutantResultRepository extends JpaRepository<SimulationPollutantResult, Long> {

    // Método para buscar resultados pelo ID do lote de simulação
    List<SimulationPollutantResult> findBySimulationBatchId(Long simulationBatchId);
}