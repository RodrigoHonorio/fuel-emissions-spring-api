package com.example.emissionsCalculator.service.scientific.simulation;

import com.example.emissionsCalculator.dto.scientific.SimulationBatchResponseDTO;
import com.example.emissionsCalculator.entity.scientific.simulation.SimulationBatch;
import com.example.emissionsCalculator.repository.scientific.simulation.SimulationBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemporalSimulationService {

    private final SimulationBatchRepository batchRepository;
    private final SimulationExecutorWorker simulationExecutorWorker;

    @Transactional
    public SimulationBatchResponseDTO runTemporalSimulation(String batchName, int forecastDays, int lookbackDays, Long parentSimulationId) {
        log.info("🚀 [ASYNC SIMULATION] A iniciar batch: '{}' | Futuro: {} dias | Passado (Lookback): {} dias", batchName, forecastDays, lookbackDays);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime historyStartTime = now.minusDays(lookbackDays);
        LocalDateTime endForecastTime = now.plusDays(forecastDays);

        SimulationBatch batch = SimulationBatch.builder()
                .batchName(batchName)
                .articleName("Scientific Rules & CSV Engine Framework")
                .executionTimestamp(now)
                .horizonType(forecastDays > 1 ? "FORECAST_" + forecastDays + "_DAYS" : "HOURLY_24H")
                .lookbackDays(lookbackDays)
                .historyStartTime(historyStartTime)
                .startForecastTime(now)
                .endForecastTime(endForecastTime)
                .parentSimulationId(parentSimulationId)
                .status("PROCESSING")
                .build();

        batch = batchRepository.save(batch);
        Long batchId = batch.getId();

        // Dispara o worker em background com a linha do tempo completa (Passado + Futuro)
        CompletableFuture.runAsync(() -> simulationExecutorWorker.executeSimulation(batchId, historyStartTime, endForecastTime, parentSimulationId));

        log.info("⚡ [ASYNC SIMULATION] Lote ID {} a processar linha do tempo completa em background.", batchId);
        return mapToDto(batch);
    }

    @Transactional(readOnly = true)
    public List<SimulationBatchResponseDTO> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SimulationBatchResponseDTO getBatchById(Long id) {
        return batchRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Batch não encontrado com ID: " + id));
    }

    private SimulationBatchResponseDTO mapToDto(SimulationBatch batch) {
        return new SimulationBatchResponseDTO(
                batch.getId(),
                batch.getBatchName(),
                batch.getArticleName(),
                batch.getExecutionTimestamp(),
                batch.getHorizonType(),
                batch.getLookbackDays(),
                batch.getHistoryStartTime(),
                batch.getStartForecastTime(),
                batch.getEndForecastTime(),
                batch.getParentSimulationId(),
                batch.getStatus()
        );
    }
}