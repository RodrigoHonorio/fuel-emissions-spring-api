package com.example.emissionsCalculator.controller.scientific;

import com.example.emissionsCalculator.dto.scientific.ScientificRuleResponseDTO;
import com.example.emissionsCalculator.engine.scientific.generalPollution.DynamicScientificEmissionModelStrategy;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.service.scientific.ScientificRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/scientific-rules")
@RequiredArgsConstructor
public class ScientificRuleController {

    private final ScientificRuleService scientificRuleService;
    private final DynamicScientificEmissionModelStrategy dynamicStrategy;

    /**
     * Endpoint para upload do CSV de regras e do PDF do artigo científico.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadRulesAndArticle(
            @RequestParam("csvFile") MultipartFile csvFile,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile) {
        try {
            int rulesProcessed = scientificRuleService.processCsvAndPdfUpload(csvFile, pdfFile);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Upload realizado com sucesso! " + rulesProcessed + " regra(s) científica(s) importada(s) e PDF guardado.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar o upload dos ficheiros científicos: " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar todas as regras científicas cadastradas (via DTO).
     */
    @GetMapping
    public ResponseEntity<List<ScientificRuleResponseDTO>> getAllRules() {
        return ResponseEntity.ok(scientificRuleService.getAllRules());
    }

    /**
     * Endpoint para simular o cálculo dinâmico utilizando o motor MVEL e as regras guardadas.
     */
    @PostMapping("/simulate")
    public ResponseEntity<ScientificModelResult> simulateCalculation(
            @RequestParam(defaultValue = "15.0") double windSpeedKmh,
            @RequestParam(defaultValue = "25.0") double temperatureCelsius,
            @RequestParam(defaultValue = "1000.0") double gasolineLiters) {

        CmaGasStation station = new CmaGasStation();
        station.setId(1L); // Definido apenas o ID necessário para a simulação

        HourlyWeatherRecord weather = new HourlyWeatherRecord();
        weather.setWindSpeedKmh(windSpeedKmh);
        weather.setTemperatureCelsius(temperatureCelsius);
        weather.setRelativeHumidityPercentage(60.0);

        ScientificModelResult result = dynamicStrategy.calculate(
                station,
                weather,
                null,
                gasolineLiters,
                0.0,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(result);
    }
}