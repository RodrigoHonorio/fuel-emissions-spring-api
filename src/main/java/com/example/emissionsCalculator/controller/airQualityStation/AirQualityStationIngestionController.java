package com.example.emissionsCalculator.controller.airQualityStation;

import com.example.emissionsCalculator.service.airQualityStation.AirQualityStationIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>O que é:</b> Controlador REST para o módulo de Qualidade do Ar.
 * <b>Para que serve:</b> Expor o endpoint POST /api/air-quality-stations/sync no ecossistema da aplicação.
 */
@RestController
@RequestMapping("/api/air-quality-stations")
@RequiredArgsConstructor
public class AirQualityStationIngestionController {

    private final AirQualityStationIngestionService service;

    /**
     * <b>O que é:</b> Endpoint de gatilho para sincronização de estações.
     * <b>Para que serve:</b> Executar a ingestão manual/agendada dos metadados das estações LAQN.
     */
    @PostMapping("/sync")
    public ResponseEntity<String> syncStations() {
        try {
            int totalProcessed = service.syncStations();
            return ResponseEntity.ok("Sincronização de estações concluída! " + totalProcessed + " estações processadas.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro durante a sincronização: " + e.getMessage());
        }
    }
}