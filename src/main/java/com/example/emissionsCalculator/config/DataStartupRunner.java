package com.example.emissionsCalculator.config;

import com.example.emissionsCalculator.service.system.MasterDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

@Slf4j
@RequiredArgsConstructor
public class DataStartupRunner implements CommandLineRunner {

    private final MasterDataSyncService masterDataSyncService;

    @Override
    public void run(String... args) {
        log.info("⚡ [BOOTSTRAP] A disparar a ingestão automática de dados no arranque...");
        try {
            masterDataSyncService.executeFullDataPipeline();
        } catch (Exception e) {
            log.error("⚠️ [BOOTSTRAP] Falha no carregamento inicial de dados: ", e);
        }
    }
}
