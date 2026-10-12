package com.example.emissionsCalculator.controller.spatial;

import com.example.emissionsCalculator.dto.spatial.HeatmapFeatureDTO;
import com.example.emissionsCalculator.dto.spatial.HotspotAnalysisResponseDTO;
import com.example.emissionsCalculator.dto.spatial.PlumePointDTO;
import com.example.emissionsCalculator.service.spatial.SpatialAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spatial")
public class SpatialAnalysisController {

    private final SpatialAnalysisService spatialAnalysisService;

    public SpatialAnalysisController(SpatialAnalysisService spatialAnalysisService) {
        this.spatialAnalysisService = spatialAnalysisService;
    }

    @GetMapping("/heatmap-data")
    public ResponseEntity<List<HeatmapFeatureDTO>> getHeatmapData() {
        return ResponseEntity.ok(spatialAnalysisService.getHeatmapData());
    }

    /**
     * Endpoint para retornar a simulação da dispersão das plumas de poluição,
     * orientada pela velocidade e direção horária do vento.
     */
    @GetMapping("/plume-dispersion")
    public ResponseEntity<List<PlumePointDTO>> getPlumeDispersion() {
        return ResponseEntity.ok(spatialAnalysisService.getPlumeDispersionData());
    }

    @GetMapping("/hotspots")
    public ResponseEntity<HotspotAnalysisResponseDTO> getHotspots(
            @RequestParam(defaultValue = "51.28") double minLat,
            @RequestParam(defaultValue = "51.69") double maxLat,
            @RequestParam(defaultValue = "-0.51") double minLon,
            @RequestParam(defaultValue = "0.33") double maxLon,
            @RequestParam(defaultValue = "0.01") double gridSize,
            @RequestParam(defaultValue = "5.0") double threshold
    ) {
        HotspotAnalysisResponseDTO result = spatialAnalysisService.analyzeHotspots(
                minLat, maxLat, minLon, maxLon, gridSize, threshold
        );
        return ResponseEntity.ok(result);
    }
}