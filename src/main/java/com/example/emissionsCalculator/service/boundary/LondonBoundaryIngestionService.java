package com.example.emissionsCalculator.service.boundary;

import com.example.emissionsCalculator.dto.boundary.OnsBoroughFeatureDTO;
import com.example.emissionsCalculator.dto.boundary.OnsBoroughResponseDTO;
import com.example.emissionsCalculator.entity.boundary.LondonBoundary;
import com.example.emissionsCalculator.service.boundary.enums.LondonBoundaryApi;
import com.example.emissionsCalculator.repository.boundary.LondonBoundaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>O que é:</b> Serviço responsável pela lógica de negócio de importação de dados geográficos.
 * <b>Para que serve:</b> Conectar à API externa da ONS, buscar os polígonos de Londres e persisti-los no PostgreSQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LondonBoundaryIngestionService {

    private final LondonBoundaryRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    @Transactional
    public int syncLondonBoundaries() {
        log.info("⏳ Baixando limites geográficos de Londres via ONS API...");

        try {
            // Busca as configurações centralizadas no enum
            LondonBoundaryApi apiConfig = LondonBoundaryApi.ONS_ARCGIS_DISTRICTS;

            OnsBoroughResponseDTO response = restClient.get()
                    .uri(apiConfig.getBaseUrl(), uriBuilder -> uriBuilder
                            .queryParam("where", apiConfig.getWhereClause())
                            .queryParam("outFields", "*")
                            .queryParam("outSR", apiConfig.getOutSr())
                            .queryParam("f", apiConfig.getFormat())
                            .build())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SpringBoot/3.x")
                    .header("Accept", "application/geo+json, application/json")
                    .retrieve()
                    .body(OnsBoroughResponseDTO.class);

            if (response == null || response.features() == null || response.features().isEmpty()) {
                log.warn("⚠️ Nenhum limite geográfico retornado pela API da ONS.");
                return 0;
            }

            List<LondonBoundary> boundariesToSave = new ArrayList<>();

            for (OnsBoroughFeatureDTO feature : response.features()) {
                if (feature.properties() != null && feature.properties().boroughCode() != null) {

                    String boroughCode = feature.properties().boroughCode();
                    String boroughName = feature.properties().boroughName();

                    // Converte o objeto de geometria em String JSON para salvar na coluna JSONB do banco
                    String geometryJson = objectMapper.writeValueAsString(feature.geometry());

                    LondonBoundary boundary = repository.findByBoroughCode(boroughCode)
                            .orElse(LondonBoundary.builder()
                                    .boroughCode(boroughCode)
                                    .build());

                    boundary.setBoroughName(boroughName);
                    boundary.setGeojsonGeometry(geometryJson);
                    boundary.setUpdatedAt(LocalDateTime.now());

                    boundariesToSave.add(boundary);
                }
            }

            repository.saveAll(boundariesToSave);
            log.info("✅ Sincronização concluída com sucesso! {} distritos gravados.", boundariesToSave.size());
            return boundariesToSave.size();

        } catch (HttpClientErrorException e) {
            log.error("❌ O ArcGIS/ONS respondeu com erro status {}. Detalhes: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erro retornado pelo ArcGIS: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("❌ Falha na ingestão dos limites geográficos de Londres: {}", e.getMessage(), e);
            throw new RuntimeException("Erro na ingestão GeoJSON da ONS: " + e.getMessage(), e);
        }
    }
}