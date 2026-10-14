package com.example.emissionsCalculator.service.system;

import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemAutoSyncManager implements CommandLineRunner {

    private final MasterDataSyncService masterDataSyncService;
    private final HourlyWeatherRecordRepository weatherRepository;

    @Override
    public void run(String... args) {
        log.info("🔍 [STARTUP GAP CHECK] A verificar integridade e lacunas temporais na base de dados...");
        try {
            LocalDateTime latestWeatherTime = weatherRepository.findFirstByOrderByForecastTimestampDesc()
                    .map(w -> w.getForecastTimestamp())
                    .orElse(null);

            LocalDateTime now = LocalDateTime.now();

            if (latestWeatherTime == null) {
                log.warn("⚠️ Nenhum registo meteorológico encontrado. A executar pipeline completa de bootstrap...");
                masterDataSyncService.executeFullDataPipeline();
            } else {
                long hoursBetween = ChronoUnit.HOURS.between(latestWeatherTime, now);
                if (hoursBetween > 2) {
                    log.warn("⚠️ Detetado um gap temporal de {} horas nos dados meteorológicos (Último: {}). A atualizar...", hoursBetween, latestWeatherTime);
                    masterDataSyncService.executeFullDataPipeline();
                } else {
                    log.info("✅ Dados meteorológicos atualizados até {}. Nenhum gap crítico detetado no arranque.", latestWeatherTime);
                }
            }
        } catch (Exception e) {
            log.error("❌ Erro ao verificar gaps meteorológicos no arranque: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Executa a cada hora exata (minuto 0, segundo 0)
    public void scheduledHourlySync() {
        log.info("⏰ [SCHEDULED SYNC] A executar sincronização horária automática do sistema...");
        try {
            masterDataSyncService.executeFullDataPipeline();
            log.info("✅ Sincronização horária executada com sucesso!");
        } catch (Exception e) {
            log.error("❌ Falha crítica na sincronização horária agendada: {}", e.getMessage());
        }
    }
}