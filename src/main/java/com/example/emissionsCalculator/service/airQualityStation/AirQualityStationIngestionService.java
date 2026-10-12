package com.example.emissionsCalculator.service.airQualityStation;

import com.example.emissionsCalculator.dto.airQualityStation.LaqnMonitoringSitesResponseDTO;
import com.example.emissionsCalculator.dto.airQualityStation.LaqnSiteDTO;
import com.example.emissionsCalculator.entity.airQualityStation.AirQualityStation;
import com.example.emissionsCalculator.repository.airQualityStation.AirQualityStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <b>O que é:</b> Serviço de ingestão de estações de qualidade do ar.
 * <b>Para que serve:</b> Sincronizar os metadados das estações da LAQN API via Spring RestClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityStationIngestionService {

    private final AirQualityStationRepository repository;
    private final RestClient restClient = RestClient.create();

    private static final String LAQN_STATIONS_API_URL = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";

    /**
     * <b>O que é:</b> Método principal de sincronização.
     * <b>Para que serve:</b> Consumir a LAQN API e persistir as estações ativas e inativas de forma idempotente.
     * @return Quantidade de estações processadas e gravadas.
     */
    @Transactional
    public int syncStations() {
        log.info("⏳ A conectar à LAQN API para sincronizar estações de qualidade do ar...");

        try {
            LaqnMonitoringSitesResponseDTO response = restClient.get()
                    .uri(LAQN_STATIONS_API_URL)
                    .retrieve()
                    .body(LaqnMonitoringSitesResponseDTO.class);

            if (response == null || response.sites() == null || response.sites().siteList() == null) {
                log.warn("⚠️ Nenhuma estação encontrada no payload retornado pela LAQN API.");
                return 0;
            }

            List<LaqnSiteDTO> siteDTOs = response.sites().siteList();

            List<String> incomingCodes = siteDTOs.stream()
                    .filter(dto -> dto.siteCode() != null && dto.latitude() != null && dto.longitude() != null)
                    .map(LaqnSiteDTO::siteCode)
                    .distinct()
                    .collect(Collectors.toList());

            List<AirQualityStation> existingStations = repository.findBySiteCodeIn(incomingCodes);
            Set<String> existingSiteCodes = existingStations.stream()
                    .map(AirQualityStation::getSiteCode)
                    .collect(Collectors.toSet());

            List<AirQualityStation> stationsToSave = new ArrayList<>();

            for (LaqnSiteDTO dto : siteDTOs) {
                if (dto.siteCode() != null && dto.latitude() != null && dto.longitude() != null) {
                    AirQualityStation station = repository.findBySiteCode(dto.siteCode())
                            .orElse(AirQualityStation.builder()
                                    .siteCode(dto.siteCode())
                                    .build());

                    station.setSiteName(dto.siteName());
                    station.setSiteType(dto.siteType());
                    station.setLatitude(dto.latitude());
                    station.setLongitude(dto.longitude());
                    station.setLatitudeWGS84(dto.latitudeWGS84());
                    station.setLongitudeWGS84(dto.longitudeWGS84());
                    station.setInServiceStatus(dto.inService());
                    station.setLocalAuthorityCode(dto.localAuthorityCode());
                    station.setLocalAuthorityName(dto.localAuthorityName());
                    station.setDataOwner(dto.dataOwner());
                    station.setDataManager(dto.dataManager());

                    // Tratamento flexível para aceitar "Yes", "true", "1" ou assumir ativo se nulo
                    boolean isActive = dto.inService() == null
                            || "Yes".equalsIgnoreCase(dto.inService().trim())
                            || "true".equalsIgnoreCase(dto.inService().trim())
                            || "1".equals(dto.inService().trim());

                    station.setIsActive(isActive);
                    station.setUpdatedAt(LocalDateTime.now());

                    stationsToSave.add(station);
                }
            }

            repository.saveAll(stationsToSave);
            log.info("✅ Sincronização concluída com sucesso! {} estações salvas/atualizadas.", stationsToSave.size());
            return stationsToSave.size();

        } catch (Exception e) {
            log.error("❌ Falha crítica ao sincronizar estações de qualidade do ar: {}", e.getMessage(), e);
            throw new RuntimeException("Erro na ingestão de dados LAQN: " + e.getMessage(), e);
        }
    }
}