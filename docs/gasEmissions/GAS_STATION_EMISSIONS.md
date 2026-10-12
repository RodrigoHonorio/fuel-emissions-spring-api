Markdown
# Módulo de Cálculo e Monitoramento de Emissões de Poluentes (EPA AP-42 / Antoine)

## 📌 Visão Geral para Pesquisadores e Analistas Ambientais

Este módulo é responsável por executar o modelo físico-químico de estimativa de emissões de **Compostos Orgânicos Voláteis (VOCs)** e **Benzeno** em postos de combustível na Região Metropolitana de Londres. O sistema cruza os dados operacionais dos postos oficiais da CMA com as séries temporais meteorológicas recentes para calcular perdas por evaporação e derramamento em tempo real.

---

## ⚗️ Fundamentação Científica e Modelos Matemáticos (EPA AP-42 & Antoine)

As equações implementadas baseiam-se nos padrões metodológicos da **EPA (U.S. Environmental Protection Agency) AP-42** para fontes de armazenamento e distribuição de combustíveis, combinadas com a **Equação de Antoine** para determinação da pressão de vapor.

### 1. Equação de Antoine (Pressão Verdadeira de Vapor)
A pressão verdadeira de vapor ($P_{vap}$) em quilopascais (kPa) é calculada em função da temperatura ambiente em Celsius ($T$):

$$P_{vap} = A \cdot \exp(B \cdot T)$$

* Onde $A$ (`ANTOINE_BASE_MULTIPLIER`) é o multiplicador base e $B$ (`ANTOINE_EXPONENT_FACTOR`) é o fator exponencial adaptado, cujos valores estão centralizados em `EmissionModelConstants.java`[cite: 9].

### 2. Fator de Emissão por Evaporação ($EF$)
O fator de emissão expresso em gramas por litro ($g/L$) é dado por:

$$EF = \frac{M_{mult} \cdot P_{vap} \cdot MW_{factor}}{T + 273.15}$$

* Onde $T + 273.15$ converte a temperatura para a escala Kelvin, operando através do método implementado em `EvaporativeEmissionsCalculator.java`[cite: 8].

### 3. Emissões Totais de VOCs e Benzeno
* **VOCs Evaporativos (kg):** $\frac{EF \cdot V_{throughput}}{1000}$[cite: 8]
* **VOCs por Derramamento / Spillage (kg):** $\frac{F_{spillage} \cdot V_{throughput}}{1000}$ (considerando fator fixo de ~0.08 g/L)[cite: 8, 9]
* **Emissões de Benzeno (g):** $(VOC_{evap} + VOC_{spillage}) \cdot 1000 \cdot F_{benzene}$ (assumindo fração de ~1% de benzeno na fase de vapor)[cite: 8, 9]

---

## 📂 Estrutura de Arquivos e Classes do Módulo

src/main/java/com/example/teste/
├── controller/gasEmissions/
│   └── GasStationEmissionController.java[cite: 4]
├── service/gasEmissions/
│   ├── StationEmissionsCalculatorService.java[cite: 10]
│   ├── calculator/
│   │   └── EvaporativeEmissionsCalculator.java[cite: 8]
│   └── constants/
│       └── EmissionModelConstants.java[cite: 9]
├── repository/gasEmissions/
│   └── GasStationEmissionRepository.java[cite: 7]
├── entity/gasEmissions/
│   └── GasStationEmission.java[cite: 6]
└── dto/gasEmissions/
└── GasStationEmissionResponseDTO.java[cite: 5]


---

## 🗄️ Modelo de Dados (PostgreSQL)

### Tabela `gas_station_emissions`

| Coluna | Tipo SQL | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGINT` (PK) | Chave primária autoincrementável[cite: 6] |
| `gas_station_id` | `BIGINT` | Identificador do posto de combustível associado[cite: 6] |
| `calculation_timestamp` | `TIMESTAMP` | Momento exato em que o cálculo foi processado[cite: 6] |
| `temperature_celsius` | `DOUBLE PRECISION` | Temperatura do ar utilizada no modelo (°C)[cite: 6] |
| `wind_speed_kmh` | `DOUBLE PRECISION` | Velocidade do vento associada (km/h)[cite: 6] |
| `estimated_daily_throughput_liters` | `DOUBLE PRECISION` | Volume diário estimado de combustível movimentado (L)[cite: 6] |
| `evaporative_voc_kg` | `DOUBLE PRECISION` | Massa de VOCs evaporados (kg)[cite: 6] |
| `spillage_voc_kg` | `DOUBLE PRECISION` | Massa de VOCs gerados por derramamento (kg)[cite: 6] |
| `total_voc_kg` | `DOUBLE PRECISION` | Soma total de VOCs emitidos (kg)[cite: 6] |
| `benzene_emissions_g` | `DOUBLE PRECISION` | Massa total de benzeno estimada (g)[cite: 6] |

---

## 💻 Código-Fonte dos Componentes

### 1. Constantes e Enums Científicos

#### `EmissionModelConstants.java`
```java
package com.example.emissionsCalculator.service.gasEmissions.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <b>O que é:</b> Enumeração de constantes físicas e químicas para o modelo de emissões evaporativas (EPA AP-42)[cite: 9].
 * <b>Para que serve:</b> Centralizar parâmetros científicos, fatores de conversão e valores padrão[cite: 9].
 */
@Getter
@RequiredArgsConstructor
public enum EmissionModelConstants {

    DEFAULT_TEMPERATURE_CELSIUS(15.0),
    DEFAULT_WIND_SPEED_KMH(10.0),
    DEFAULT_THROUGHPUT_LITERS_PER_DAY(5000.0),

    SPILLAGE_FACTOR_G_PER_L(0.08),
    BENZENE_FRACTION_IN_VAPOR(0.01),
    CELSIUS_TO_KELVIN_OFFSET(273.15),
    GRAMS_TO_KILOGRAMS_DIVISOR(1000.0),

    ANTOINE_BASE_MULTIPLIER(10.0),
    ANTOINE_EXPONENT_FACTOR(0.04),
    EMISSION_FACTOR_MULTIPLIER(0.120),
    EMISSION_FACTOR_MOLECULAR_WEIGHT_FACTOR(66.0);

    private final double value;
}
2. Calculador Isolado (Pure Java Logic)
EvaporativeEmissionsCalculator.java
Java
package com.example.emissionsCalculator.service.gasEmissions.calculator;

import com.example.emissionsCalculator.enums.gasEmissions.EmissionModelConstants;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Calculador físico-químico isolado para emissões evaporativas[cite: 8].
 * <b>Para que serve:</b> Executar as equações matemáticas da EPA AP-42 sem acoplamento com o Spring[cite: 8].
 */
public class EvaporativeEmissionsCalculator {

    private EvaporativeEmissionsCalculator() {}

    public static double calculateTrueVaporPressure(double temperatureCelsius) {
        return EmissionModelConstants.ANTOINE_BASE_MULTIPLIER.getValue()
                * Math.exp(EmissionModelConstants.ANTOINE_EXPONENT_FACTOR.getValue() * temperatureCelsius);
    }

    public static double calculateEmissionFactor(double temperatureCelsius, double trueVaporPressure) {
        double tempKelvin = temperatureCelsius + EmissionModelConstants.CELSIUS_TO_KELVIN_OFFSET.getValue();
        return (EmissionModelConstants.EMISSION_FACTOR_MULTIPLIER.getValue()
                * trueVaporPressure
                * EmissionModelConstants.EMISSION_FACTOR_MOLECULAR_WEIGHT_FACTOR.getValue()) / tempKelvin;
    }

    public static GasStationEmission calculateForStation(long stationId,
                                                         double tempC,
                                                         double windKmh,
                                                         double throughputLiters,
                                                         double emissionFactor,
                                                         LocalDateTime timestamp) {

        double evaporativeVocKg = (emissionFactor * throughputLiters)
                / EmissionModelConstants.GRAMS_TO_KILOGRAMS_DIVISOR.getValue();

        double spillageVocKg = (EmissionModelConstants.SPILLAGE_FACTOR_G_PER_L.getValue() * throughputLiters)
                / EmissionModelConstants.GRAMS_TO_KILOGRAMS_DIVISOR.getValue();

        double totalVocKg = evaporativeVocKg + spillageVocKg;

        double benzeneGrams = totalVocKg
                * EmissionModelConstants.GRAMS_TO_KILOGRAMS_DIVISOR.getValue()
                * EmissionModelConstants.BENZENE_FRACTION_IN_VAPOR.getValue();

        return GasStationEmission.builder()
                .gasStationId(stationId)
                .calculationTimestamp(timestamp)
                .temperatureCelsius(tempC)
                .windSpeedKmh(windKmh)
                .estimatedDailyThroughputLiters(throughputLiters)
                .evaporativeVocKg(evaporativeVocKg)
                .spillageVocKg(spillageVocKg)
                .totalVocKg(totalVocKg)
                .benzeneEmissionsG(benzeneGrams)
                .build();
    }
}
3. Entidade JPA
GasStationEmission.java
Java
package com.example.emissionsCalculator.entity.gasEmissions;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Entidade JPA para registo dos cálculos de emissões evaporativas[cite: 6].
 * <b>Para que serve:</b> Armazenar os resultados do modelo físico-químico na base de dados[cite: 6].
 */
@Entity
@Table(name = "gas_station_emissions", indexes = {
        @Index(name = "idx_emissions_station_time", columnList = "gas_station_id, calculation_timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GasStationEmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_station_id", nullable = false)
    private Long gasStationId;

    @Column(name = "calculation_timestamp", nullable = false)
    private LocalDateTime calculationTimestamp;

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

    @Column(name = "wind_speed_kmh")
    private Double windSpeedKmh;

    @Column(name = "estimated_daily_throughput_liters", nullable = false)
    private Double estimatedDailyThroughputLiters;

    @Column(name = "evaporative_voc_kg", nullable = false)
    private Double evaporativeVocKg;

    @Column(name = "spillage_voc_kg", nullable = false)
    private Double spillageVocKg;

    @Column(name = "total_voc_kg", nullable = false)
    private Double totalVocKg;

    @Column(name = "benzene_emissions_g", nullable = false)
    private Double benzeneEmissionsG;
}
4. DTO e Repositório
GasStationEmissionResponseDTO.java
Java
package com.example.emissionsCalculator.dto.gasEmissions;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Record DTO de resposta para exibição dos cálculos de emissão[cite: 5].
 */
public record GasStationEmissionResponseDTO(
        Long id,
        Long gasStationId,
        LocalDateTime calculationTimestamp,
        Double temperatureCelsius,
        Double windSpeedKmh,
        Double estimatedDailyThroughputLiters,
        Double evaporativeVocKg,
        Double spillageVocKg,
        Double totalVocKg,
        Double benzeneEmissionsG
) {}
GasStationEmissionRepository.java
Java
package com.example.emissionsCalculator.repository.gasEmissions;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <b>O que é:</b> Repositório JPA para a entidade GasStationEmission[cite: 7].
 * <b>Para que serve:</b> Realizar operações de persistência e consultas na tabela 'gas_station_emissions'[cite: 7].
 */
@Repository
public interface GasStationEmissionRepository extends JpaRepository<GasStationEmission, Long> {

    List<GasStationEmission> findByGasStationIdOrderByCalculationTimestampDesc(Long gasStationId);

    @Query(value = """
        SELECT DISTINCT ON (gas_station_id) *
        FROM gas_station_emissions
        ORDER BY gas_station_id, calculation_timestamp DESC
        """, nativeQuery = true)
    List<GasStationEmission> findLatestEmissionsForAllStations();
}
5. Service e Controller Layer
StationEmissionsCalculatorService.java
Java
package com.example.emissionsCalculator.service.gasEmissions;

import com.example.emissionsCalculator.service.gasEmissions.calculator.EvaporativeEmissionsCalculator;
import com.example.emissionsCalculator.enums.gasEmissions.EmissionModelConstants;
import com.example.emissionsCalculator.dto.gasEmissions.GasStationEmissionResponseDTO;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.gasEmissions.GasStationEmissionRepository;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <b>O que é:</b> Serviço responsável pela orquestração do cálculo de emissões[cite: 10].
 * <b>Para que serve:</b> Buscar variáveis climáticas recentes, iterar sobre os postos da CMA e persistir de forma assíncrona[cite: 10].
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationEmissionsCalculatorService {

    private final GasStationEmissionRepository emissionRepository;
    private final HourlyWeatherRecordRepository weatherRepository;
    private final CmaGasStationRepository cmaGasStationRepository;

    @Async
    @Transactional
    public CompletableFuture<Integer> calculateEmissionsForCurrentHour() {
        log.info("⏳ Iniciando orquestração do cálculo de emissões...");
        try {
            HourlyWeatherRecord latestWeather = weatherRepository.findFirstByOrderByForecastTimestampDesc()
                    .or(() -> weatherRepository.findFirstByOrderByIdDesc())
                    .orElse(null);

            double tempC = (latestWeather != null && latestWeather.getTemperatureCelsius() != null)
                    ? latestWeather.getTemperatureCelsius()
                    : EmissionModelConstants.DEFAULT_TEMPERATURE_CELSIUS.getValue();

            double windKmh = (latestWeather != null && latestWeather.getWindSpeedKmh() != null)
                    ? latestWeather.getWindSpeedKmh()
                    : EmissionModelConstants.DEFAULT_WIND_SPEED_KMH.getValue();

            double trueVaporPressure = EvaporativeEmissionsCalculator.calculateTrueVaporPressure(tempC);
            double emissionFactor = EvaporativeEmissionsCalculator.calculateEmissionFactor(tempC, trueVaporPressure);

            List<CmaGasStation> cmaStations = cmaGasStationRepository.findAll();
            if (cmaStations.isEmpty()) {
                return CompletableFuture.completedFuture(0);
            }

            List<GasStationEmission> emissionsToSave = new ArrayList<>(cmaStations.size());
            LocalDateTime now = LocalDateTime.now();

            for (CmaGasStation station : cmaStations) {
                double throughputLiters = EmissionModelConstants.DEFAULT_THROUGHPUT_LITERS_PER_DAY.getValue();
                GasStationEmission emission = EvaporativeEmissionsCalculator.calculateForStation(
                        station.getId(), tempC, windKmh, throughputLiters, emissionFactor, now
                );
                emissionsToSave.add(emission);
            }

            List<GasStationEmission> savedEmissions = emissionRepository.saveAll(emissionsToSave);
            return CompletableFuture.completedFuture(savedEmissions.size());

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional(readOnly = true)
    public Page<GasStationEmissionResponseDTO> getEmissions(Pageable pageable) {
        return emissionRepository.findAll(pageable)
                .map(e -> new GasStationEmissionResponseDTO(
                        e.getId(), e.getGasStationId(), e.getCalculationTimestamp(),
                        e.getTemperatureCelsius(), e.getWindSpeedKmh(),
                        e.getEstimatedDailyThroughputLiters(), e.getEvaporativeVocKg(),
                        e.getSpillageVocKg(), e.getTotalVocKg(), e.getBenzeneEmissionsG()
                ));
    }

    @Transactional(readOnly = true)
    public List<GasStationEmissionResponseDTO> getLatestEmissionsForMap() {
        return emissionRepository.findLatestEmissionsForAllStations().stream()
                .map(e -> new GasStationEmissionResponseDTO(
                        e.getId(), e.getGasStationId(), e.getCalculationTimestamp(),
                        e.getTemperatureCelsius(), e.getWindSpeedKmh(),
                        e.getEstimatedDailyThroughputLiters(), e.getEvaporativeVocKg(),
                        e.getSpillageVocKg(), e.getTotalVocKg(), e.getBenzeneEmissionsG()
                ))
                .toList();
    }
}
GasStationEmissionController.java
Java
package com.example.emissionsCalculator.controller.gasEmissions;

import com.example.emissionsCalculator.dto.gasEmissions.GasStationEmissionResponseDTO;
import com.example.emissionsCalculator.engine.gasEmissions.StationEmissionsCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <b>O que é:</b> Controlador REST para o módulo de Cálculo de Emissões[cite: 4].
 * <b>Para que serve:</b> Expor endpoints para acionamento do cálculo e consulta paginada ou atualizada[cite: 4].
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/emissions")
@RequiredArgsConstructor
public class GasStationEmissionController {

    private final StationEmissionsCalculatorService emissionsService;

    @PostMapping("/calculate")
    public ResponseEntity<String> calculateEmissions() {
        try {
            emissionsService.calculateEmissionsForCurrentHour();
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Cálculo de emissões iniciado em segundo plano com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao iniciar o cálculo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<GasStationEmissionResponseDTO>> getEmissions(Pageable pageable) {
        return ResponseEntity.ok(emissionsService.getEmissions(pageable));
    }

    @GetMapping("/map-latest")
    public ResponseEntity<List<GasStationEmissionResponseDTO>> getLatestEmissionsForMap() {
        return ResponseEntity.ok(emissionsService.getLatestEmissionsForMap());
    }
}
🔍 Consultas SQL de Validação
1. Últimas Emissões Calculadas por Posto
SQL
SELECT DISTINCT ON (gas_station_id) 
    gas_station_id, calculation_timestamp, temperature_celsius, total_voc_kg, benzene_emissions_g
FROM gas_station_emissions
ORDER BY gas_station_id, calculation_timestamp DESC;
2. Média de Emissões Totais de VOCs na Cidade
SQL
SELECT AVG(total_voc_kg) AS media_voc_kg, AVG(benzene_emissions_g) AS media_benzeno_g
FROM gas_station_emissions;