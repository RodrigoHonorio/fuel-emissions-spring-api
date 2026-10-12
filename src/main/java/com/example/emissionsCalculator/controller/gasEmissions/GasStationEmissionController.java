package com.example.emissionsCalculator.controller.gasEmissions;

import com.example.emissionsCalculator.dto.gasEmissions.GasStationEmissionResponseDTO;
import com.example.emissionsCalculator.service.gasEmissions.StationEmissionsCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <b>O que é:</b> Controlador REST para o módulo de Cálculo de Emissões de Postos.
 * <b>Para que serve:</b> Expor endpoints para acionamento do cálculo e consulta paginada ou atualizada dos resultados.
 */
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

    /**
     * Retorna os registros de emissão de forma paginada para evitar sobrecarga no frontend/Swagger.
     */
    @GetMapping
    public ResponseEntity<Page<GasStationEmissionResponseDTO>> getEmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(emissionsService.getEmissions(pageRequest));
    }

    /**
     * Retorna apenas a medição mais recente de emissões para cada posto (ideal para renderização em mapas).
     */
    @GetMapping("/map-latest")
    public ResponseEntity<List<GasStationEmissionResponseDTO>> getLatestEmissionsForMap() {
        return ResponseEntity.ok(emissionsService.getLatestEmissionsForMap());
    }
}