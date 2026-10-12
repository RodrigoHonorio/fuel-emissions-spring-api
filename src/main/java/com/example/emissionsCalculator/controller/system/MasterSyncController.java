package com.example.emissionsCalculator.controller.system;

import com.example.emissionsCalculator.service.system.MasterDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class MasterSyncController {

    private final MasterDataSyncService masterDataSyncService;

    @PostMapping("/sync-all")
    public ResponseEntity<String> syncAllSystemData() {
        try {
            String report = masterDataSyncService.executeFullDataPipeline();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao executar carga completa: " + e.getMessage());
        }
    }

    @PostMapping("/sync-all-json")
    public ResponseEntity<Map<String, Object>> syncAllSystemDataJson() {
        try {
            Map<String, Object> report = masterDataSyncService.executeFullPipelineMap();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, Object> errorReport = new HashMap<>();
            errorReport.put("status", "ERROR");
            errorReport.put("message", "Erro ao executar carga completa: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorReport);
        }
    }
}