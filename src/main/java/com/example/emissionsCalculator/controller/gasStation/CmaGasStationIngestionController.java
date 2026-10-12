package com.example.emissionsCalculator.controller.gasStation;

import com.example.emissionsCalculator.service.gasStation.CmaGasStationIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>O que é:</b> Controlador REST de sincronização dos postos da CMA[cite: 22].
 * <b>Para que serve:</b> Expor o endpoint POST sem parâmetros no Swagger para sincronização dos dados governamentais da CMA.
 * <b>De onde veio:</b> Spring MVC / OpenAPI Swagger UI[cite: 27].
 */
@RestController
@RequestMapping("/api/cma-gas-stations")
@RequiredArgsConstructor
public class CmaGasStationIngestionController {

    /** Serviço de ingestão da CMA[cite: 22]. */
    private final CmaGasStationIngestionService service;

    /**
     * <b>O que é:</b> Endpoint de sincronização da CMA[cite: 10, 15].
     * <b>Para que serve:</b> Disparar o processamento e retornar mensagem de sucesso/erro padronizada[cite: 15, 22].
     * <b>De onde veio:</b> Chamada HTTP POST no caminho `/api/cma-gas-stations/sync`[cite: 10, 15].
     *
     * @return ResponseEntity com mensagem do status da operação.
     */
    @PostMapping("/sync")
    public ResponseEntity<String> syncStations() {
        try {
            int count = service.syncCmaGasStations();
            return ResponseEntity.ok("Sincronização da CMA concluída com sucesso! " + count + " postos gravados na base de dados.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao sincronizar dados da CMA: " + e.getMessage());
        }
    }
}