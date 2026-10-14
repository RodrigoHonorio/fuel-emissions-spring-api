package com.example.emissionsCalculator.controller.scientific;

import com.example.emissionsCalculator.dto.scientific.SimulationBatchResponseDTO;
import com.example.emissionsCalculator.service.scientific.simulation.TemporalSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
public class SimulationBatchController {

    private final TemporalSimulationService temporalSimulationService;

    @PostMapping("/run")
    public ResponseEntity<SimulationBatchResponseDTO> runSimulation(
            @RequestParam String batchName,
            @RequestParam(defaultValue = "16") int forecastDays,
            @RequestParam(defaultValue = "0") int lookbackDays,
            @RequestParam(required = false) Long parentSimulationId) {

        SimulationBatchResponseDTO responseDTO = temporalSimulationService.runTemporalSimulation(batchName, forecastDays, lookbackDays, parentSimulationId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDTO);
    }

    @GetMapping("/batches")
    public ResponseEntity<List<SimulationBatchResponseDTO>> getAllBatches() {
        return ResponseEntity.ok(temporalSimulationService.getAllBatches());
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<SimulationBatchResponseDTO> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(temporalSimulationService.getBatchById(id));
    }
}