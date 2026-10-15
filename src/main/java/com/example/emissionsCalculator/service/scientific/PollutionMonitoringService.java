package com.example.emissionsCalculator.service.scientific;

import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.entity.spatial.SpatialHotspot;
import com.example.emissionsCalculator.repository.scientific.vocGasStation.ScientificModelResultRepository;
import com.example.emissionsCalculator.repository.spatial.SpatialHotspotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollutionMonitoringService {

    private final ScientificModelResultRepository modelResultRepository;
    private final SpatialHotspotRepository spatialHotspotRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private static final double VOC_CRITICAL_THRESHOLD = 50.0;
    private static final double VOC_WARNING_THRESHOLD = 35.0;

    // Executa a cada 1 hora automaticamente
    @Scheduled(fixedRate = 3600000)
    public void evaluateEmissionsAndCreateHotspots() {
        log.info("[MONITORING] A avaliar emissões e a atualizar hotspots espaciais...");

        List<ScientificModelResult> recentResults = modelResultRepository.findAll();

        for (ScientificModelResult result : recentResults) {
            double totalVoc = result.getCalculatedTotalVocKg() != null ? result.getCalculatedTotalVocKg() : 0.0;

            if (totalVoc >= VOC_CRITICAL_THRESHOLD) {
                saveHotspot(result, "CRITICAL", totalVoc);
            } else if (totalVoc >= VOC_WARNING_THRESHOLD) {
                saveHotspot(result, "WARNING", totalVoc);
            }
        }
    }

    private void saveHotspot(ScientificModelResult result, String severity, double intensity) {
        // Exemplo de coordenadas centrais de Londres (ou associadas ao posto correspondente)
        Point point = geometryFactory.createPoint(new Coordinate(-0.1278, 51.5074));

        SpatialHotspot hotspot = SpatialHotspot.builder()
                .pollutantType("VOC_TOTAL")
                .calculationTimestamp(LocalDateTime.now())
                .intensityScore(intensity)
                .severityLevel(severity)
                .sourceInstitution("SPIRE-EPA-Model")
                .standardReference("DEFRA-OMS-Threshold")
                .geometry(point)
                .boroughCode("WESTMINSTER") // Pode ser dinâmico consoante o posto
                .build();

        spatialHotspotRepository.save(hotspot);

        log.warn("🚨 [HOTSPOT ESPACIAL DETETADO] Severidade: {} | Poluente: VOC_TOTAL | Intensidade: {} kg | Borough: WESTMINSTER",
                severity, intensity);
    }
}