package com.example.emissionsCalculator.controller.spatial;

import com.example.emissionsCalculator.dto.spatial.HeatmapFeatureDTO;
import com.example.emissionsCalculator.dto.spatial.HotspotAnalysisResponseDTO;
import com.example.emissionsCalculator.dto.spatial.HotspotResponseDTO;
import com.example.emissionsCalculator.dto.spatial.PlumePointDTO;
import com.example.emissionsCalculator.service.spatial.SpatialAnalysisService;
import com.example.emissionsCalculator.service.spatial.SpatialHotspotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spatial")
@RequiredArgsConstructor
public class SpatialAnalysisController {

    private final SpatialAnalysisService spatialAnalysisService;
    private final SpatialHotspotService spatialHotspotService;

    @GetMapping("/heatmap-data")
    public ResponseEntity<List<HeatmapFeatureDTO>> getHeatmapData() {
        return ResponseEntity.ok(spatialAnalysisService.getHeatmapData());
    }

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

    @GetMapping("/hotspots-db")
    public ResponseEntity<List<HotspotResponseDTO>> getHotspotsFromDb() {
        return ResponseEntity.ok(spatialHotspotService.getAllHotspotsFromDb());
    }
}