package com.example.emissionsCalculator.controller.boundary;

import com.example.emissionsCalculator.service.boundary.LondonBoundaryIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>O que é:</b> Controlador REST para acionamento da carga de dados territoriais.
 * <b>Para que serve:</b> Expor o endpoint POST /api/london-boundaries/sync para disparar manualmente a sincronização com a ONS.
 */
@RestController
@RequestMapping("/api/london-boundaries")
@RequiredArgsConstructor
public class LondonBoundaryIngestionController {

    private final LondonBoundaryIngestionService service;

    @PostMapping("/sync")
    public ResponseEntity<String> syncBoundaries() {
        try {
            int totalProcessed = service.syncLondonBoundaries();
            return ResponseEntity.ok("Sincronização de limites concluída! " + totalProcessed + " distritos processados.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro durante a sincronização: " + e.getMessage());
        }
    }
}