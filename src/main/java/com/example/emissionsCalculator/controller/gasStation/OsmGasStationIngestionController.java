package com.example.emissionsCalculator.controller.gasStation;

import com.example.emissionsCalculator.service.gasStation.OsmGasStationIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>O que é:</b> Controlador REST de sincronização de postos do OpenStreetMap (OSM)[cite: 24].
 * <b>Para que serve:</b> Expor o endpoint POST limpo sem parâmetros no Swagger para sincronização de postos de Londres[cite: 10, 15].
 * <b>De onde veio:</b> Spring MVC / OpenAPI Swagger UI[cite: 27].
 */
@RestController
@RequestMapping("/api/gas-stations")
@RequiredArgsConstructor
public class OsmGasStationIngestionController {

    /** Serviço de ingestão do OpenStreetMap[cite: 24]. */
    private final OsmGasStationIngestionService service;

    /**
     * <b>O que é:</b> Endpoint de sincronização de Londres[cite: 10, 15].
     * <b>Para que serve:</b> Disparar a busca no Overpass API por Bounding Box em Londres sem solicitar parâmetros no Swagger[cite: 10, 15, 24].
     * <b>De onde veio:</b> Chamada HTTP POST no caminho `/api/gas-stations/sync`[cite: 10, 15].
     *
     * @return ResponseEntity com contagem de postos de Londres gravados.
     */
    @PostMapping("/sync")
    public ResponseEntity<String> syncStations() {
        try {
            int count = service.syncOsmGasStationsForLondon();
            return ResponseEntity.ok("Sincronização concluída com sucesso! " + count + " postos de Londres guardados na base de dados.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao sincronizar: " + e.getMessage());
        }
    }
}
