package com.example.emissionsCalculator.controller.gasEmissions;

import com.example.emissionsCalculator.dto.gasEmissions.GasStationEmissionResponseDTO;
import com.example.emissionsCalculator.engine.gasEmissions.StationEmissionsCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/emissions")
@RequiredArgsConstructor
public class GasStationEmissionController {

    private final StationEmissionsCalculatorService emissionsService;

    @PostMapping("/calculate")
    public ResponseEntity<String> calculateEmissions() {
        try {
            emissionsService.calculateEmissionsForCurrentHour();
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Cálculo de emissões iniciado em segundo plano com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao iniciar o cálculo de emissões: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<GasStationEmissionResponseDTO>> getEmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(emissionsService.getEmissions(pageRequest));
    }

    @GetMapping("/map-latest")
    public ResponseEntity<List<GasStationEmissionResponseDTO>> getLatestEmissionsForMap() {
        return ResponseEntity.ok(emissionsService.getLatestEmissionsForMap());
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<GasStationEmissionResponseDTO>> getEmissionsForWeek(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(emissionsService.getEmissionsForWeek(startDate, endDate));
    }
}