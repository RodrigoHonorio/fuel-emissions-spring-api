package com.example.emissionsCalculator.service.system;

import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.repository.scientific.vocGasStation.ScientificModelResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final ScientificModelResultRepository resultRepository;

    public void writeScientificResultsCsv(PrintWriter writer) {
        List<ScientificModelResult> results = resultRepository.findAll();

        // Cabeçalho do CSV
        writer.println("ID,Posto ID,Modelo,Timestamp,Temperatura (C),Gasolina (L),Etanol (L),Baseline VOC (kg),Refueling VOC (kg),Total VOC (kg),Delta VOC (kg),Delta (%)");

        // Linhas de dados
        for (ScientificModelResult r : results) {
            writer.printf("%d,%d,%s,%s,%.2f,%.2f,%.2f,%.4f,%.4f,%.4f,%.4f,%.2f%%\n",
                    r.getId(),
                    r.getGasStationId(),
                    r.getModelName(),
                    r.getCalculationTimestamp(),
                    r.getTemperatureCelsius(),
                    r.getGasolineThroughputLiters(),
                    r.getEthanolThroughputLiters(),
                    r.getBaselineVocKg(),
                    r.getRefuelingVocKg(),
                    r.getCalculatedTotalVocKg(),
                    r.getDeltaVocKg(),
                    r.getDeltaPercentage()
            );
        }
    }
}
