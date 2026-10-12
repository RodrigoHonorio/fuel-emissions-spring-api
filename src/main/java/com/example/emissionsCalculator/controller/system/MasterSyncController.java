package com.example.emissionsCalculator.controller.system;

import com.example.emissionsCalculator.service.system.MasterDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}