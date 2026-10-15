package com.example.emissionsCalculator.controller.spatial;

import com.example.emissionsCalculator.dto.spatial.HotspotResponseDTO;
import com.example.emissionsCalculator.service.spatial.SpatialHotspotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotspots")
@RequiredArgsConstructor
public class SpatialHotspotController {

    private final SpatialHotspotService spatialHotspotService;

    /**
     * Retorna todos os hotspots detetados no sistema em formato DTO.
     */
    @GetMapping
    public ResponseEntity<List<HotspotResponseDTO>> getAllHotspots() {
        List<HotspotResponseDTO> hotspots = spatialHotspotService.getAllHotspotsFromDb();
        return ResponseEntity.ok(hotspots);
    }

    /**
     * Retorna apenas os hotspots filtrados por nível de severidade (Ex: CRITICAL, WARNING, DANGER).
     */
    @GetMapping("/severity/{level}")
    public ResponseEntity<List<HotspotResponseDTO>> getHotspotsBySeverity(@PathVariable String level) {
        List<HotspotResponseDTO> hotspots = spatialHotspotService.getHotspotsBySeverity(level);
        return ResponseEntity.ok(hotspots);
    }

    /**
     * Retorna os hotspots filtrados por distrito (Borough).
     */
    @GetMapping("/borough/{boroughCode}")
    public ResponseEntity<List<HotspotResponseDTO>> getHotspotsByBorough(@PathVariable String boroughCode) {
        List<HotspotResponseDTO> hotspots = spatialHotspotService.getHotspotsByBorough(boroughCode);
        return ResponseEntity.ok(hotspots);
    }

    /**
     * Endpoint para forçar manualmente o recálculo e salvamento de novos hotspots espaciais.
     */
    @PostMapping("/generate")
    public ResponseEntity<String> triggerHotspotGeneration() {
        try {
            int count = spatialHotspotService.generateAndSaveHotspots();
            return ResponseEntity.ok("Geração manual de hotspots concluída com sucesso! Total gerado: " + count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao gerar hotspots: " + e.getMessage());
        }
    }
}