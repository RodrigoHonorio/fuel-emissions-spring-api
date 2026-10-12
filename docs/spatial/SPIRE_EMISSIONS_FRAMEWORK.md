# Documentação Técnica e Científica: Framework S.P.I.R.E. e Modelo Experimental de Emissões de Abastecimento (`SPIRE_EMISSIONS_FRAMEWORK.md`)



---

## 1. Visão Geral para Pesquisadores e Analistas Ambientais



### 1.1. Contexto e Relevância Científica



Nos inventários tradicionais de emissões atmosféricas urbanas (como a metodologia padrão baseada na norma EPA AP-42), contabilizam-se prioritariamente as emissões de escapamento e as perdas evaporativas do sistema de combustível do veículo em repouso ou movimento (*Diurnal & Hot Soak Losses*).

No entanto, o processo de transferência de combustível da bomba do posto para o tanque do veículo gera **perdas por deslocamento de vapor** (*Refueling Losses*). À medida que o combustível líquido entra no reservatório, o ar saturado de vapores de Compostos Orgânicos Voláteis (VOCs) acumulado no interior do tanque é expulso diretamente para a atmosfera.

O módulo **S.P.I.R.E. (System for Pollution & Integrated Refueling Emissions)** integra a metodologia experimental desenvolvida pela **CETESB (Companhia Ambiental do Estado de São Paulo, 2015)** ao ecossistema georreferenciado da Região Metropolitana de Londres. O objetivo é reestimar e auditar o verdadeiro impacto dos postos de combustível na qualidade do ar e na formação de ozônio troposférico ($O_3$).

### 1.2. Dicionário de Atributos do Modelo Experimental



| Atributo / Coluna SQL | Definição Científica / Operacional | Unidade |
| --- | --- | --- |
| `gas_station_id` | Identificador único do posto de combustível analisado na base da CMA/OSM.

| `BIGINT` |
| `model_name` | Código identificador da estratégia científica executada (ex: `CETESB_2015_REFUELING`).

| `VARCHAR` |
| `temperature_celsius` | Temperatura do ar no momento do cálculo obtida da estação meteorológica mais próxima.

| °C |
| `baseline_voc_kg` | Emissão de referência diária calculada pelo modelo padrão EPA AP-42.

| kg/dia |
| `refueling_voc_kg` | Acréscimo de massa de VOCs gerado exclusivamente pela operação de abastecimento.

| kg/dia |
| `calculated_total_voc_kg` | Emissão total reestimada ($\text{Calculated} = \text{Baseline} + \text{Refueling}$).

| kg/dia |
| `delta_percentage` | Variação percentual de aumento na estimativa ($\Delta_{\%}$).

| % |

---

## 2. Fundamentação Científica e Modelagem Matemática



### 2.1. Equação Fundamental da CETESB (2015)



A massa anual ou diária de vapor emitida durante a operação de reabastecimento veicular ($T_g$) é calculada pela relação linear entre o volume de combustível transferido e a taxa de evaporação específica levantada experimentalmente em câmara SHED (*Sealed Housing for Evaporative Determination*):

$$T_g = C \cdot C_e$$

**Onde:**

* $T_g$: Taxa total de emissão de vapor gerado no abastecimento (expresso em gramas, $g$).


* $C$: Volume de combustível transferido durante a operação de reabastecimento (expresso em litros, $L$).


* $C_e$: Taxa de evaporação experimental específica do combustível (expresso em $g/L$).



### 2.2. Fatores de Evaporação Experimentais ($C_e$)



Os fatores de emissão foram determinados em laboratório com detectores de ionização de chama calibrados:

| Tipo de Combustível | Taxa de Evaporação ($C_e$)

| Padrão e Especificação Técnica

|
| --- | --- | --- |
| **Gasolina C** | $1{,}14 \text{ g/L}$<br> | Gasolina comercial automotiva contendo mistura com etanol anidro.

|
| **Etanol Hidratado** | $0{,}37 \text{ g/L}$<br> | Etanol combustível comercial hidratado.

|

### 2.3. Formulação dos Deltas e Comparação com a Base Neutra EPA AP-42



A base neutra (padrão estático EPA AP-42) estima perdas por evaporação em repouso combinando a Equação de Antoine para pressão verdadeira de vapor ($P_{\text{vap}}$):

$$P_{\text{vap}} = A \cdot \exp(B \cdot T)$$

$$\Delta_{\text{VOC}} = \text{Refueling}_{\text{VOC}} = \frac{(C_{\text{gasolina}} \cdot C_{e,\text{gasolina}}) + (C_{\text{etanol}} \cdot C_{e,\text{etanol}})}{1000}$$

$$\Delta_{\%} = \left( \frac{\Delta_{\text{VOC}}}{\text{Baseline}_{\text{VOC}}} \right) \times 100$$

Os ensaios demonstraram que a adição do fator de abastecimento eleva a estimativa total de VOCs urbanos entre $+22\%$ e $+183\%$ dependendo do volume comercializado e da temperatura local.

---

## 3. Visão Técnica: Arquitetura de Software e Tecnologias Utilizadas



```text
[ Scheduled Trigger / REST API Call ]
                  │
                  ▼
[ StationEmissionsEngine ] ─────────── (Injeta dinamicamente todas as estratégias ativas)
                  │
                  ├──────────────────────────────┐
                  ▼                              ▼
    [ CetesbEmissionModel ]           [ Futuros Modelos (ex: Stage II) ]
   (@ConditionalOnProperty)               (@ConditionalOnProperty)
                  │                              │
                  └──────────────┬───────────────┘
                                 ▼
                 [ ScientificModelResult ] ─── (Persistência Isolada em Lote)
                                 │
                                 ▼
                 [ PostgreSQL + PostGIS ] ──── (Tabela: scientific_model_results)

```

### 3.1. Tecnologias e Justificativas de Escolha



* **Java 17 & Spring Boot 3:** Plataforma robusta com suporte nativo a concorrência assíncrona (`CompletableFuture`), tipagem forte e injeção de dependências modular.


* **PostgreSQL + PostGIS:** Permite o armazenamento de séries temporais de simulação acopladas a consultas espaciais de contenção geográfica (`ST_Contains`) em tempo $O(\log N)$.


* **Strategy Design Pattern:** A interface `EmissionModelStrategy` desacopla a regra de cálculo da infraestrutura da aplicação. Novos modelos acadêmicos podem ser adicionados sem alterar o código existente.


* **Feature Toggle Declarativo (`@ConditionalOnProperty`):** Permite ativar ou desativar simulações específicas via arquivo `application.properties` sem necessidade de recompilar o código.


* **Data Layer Isolation:** Preserva as tabelas oficiais de domínio (`gas_stations` e `gas_station_emissions`) imutáveis, gravando simulações experimentais na tabela isolada `scientific_model_results`.



---

## 4. Estrutura de Código e Componentes do Módulo



### 4.1. Interface de Estratégia (`EmissionModelStrategy.java`)

```java
package com.example.emissionsCalculator.scientific.vocGasStation.engine;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.scientific.vocGasStation.entity.ScientificModelResult;

import java.util.List;

/**
 * Contrato universal para estratégias científicas de cálculo de emissões.
 */
public interface EmissionModelStrategy {

    String getModelName();

    List<ScientificModelResult> calculate(
            List<CmaGasStation> stations,
            HourlyWeatherRecord weather,
            List<GasStationEmission> baselineEmissions
    );
}

```

### 4.2. Implementação do Modelo CETESB 2015 (`CetesbEmissionModel.java`)

```java
package com.example.emissionsCalculator.scientific.vocGasStation.models;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.scientific.vocGasStation.engine.EmissionModelStrategy;
import com.example.emissionsCalculator.scientific.vocGasStation.entity.ScientificModelResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Estratégia de cálculo baseada no relatório CETESB (2015) para perdas no abastecimento.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "scientific.model.cetesb2015.enabled", havingValue = "true")
public class CetesbEmissionModel implements EmissionModelStrategy {

    public static final String MODEL_NAME = "CETESB_2015_REFUELING";

    private static final double EVAPORATION_RATE_GASOLINE_G_PER_L = 1.14;
    private static final double EVAPORATION_RATE_ETHANOL_G_PER_L = 0.37;

    private static final double DEFAULT_GASOLINE_THROUGHPUT_LITERS = 4500.0;
    private static final double DEFAULT_ETHANOL_THROUGHPUT_LITERS = 500.0;

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    @Override
    public List<ScientificModelResult> calculate(
            List<CmaGasStation> stations,
            HourlyWeatherRecord weather,
            List<GasStationEmission> baselineEmissions
    ) {
        log.info("📊 Executando modelo CETESB 2015 para {} postos da CMA...", stations.size());

        Map<Long, GasStationEmission> baselineMap = baselineEmissions.stream()
                .collect(Collectors.toMap(GasStationEmission::getGasStationId, e -> e));

        List<ScientificModelResult> results = new ArrayList<>(stations.size());
        LocalDateTime now = LocalDateTime.now();
        double tempC = (weather != null && weather.getTemperatureCelsius() != null)
                ? weather.getTemperatureCelsius() : 15.0;

        for (CmaGasStation station : stations) {
            GasStationEmission base = baselineMap.get(station.getId());
            double baselineVocKg = (base != null) ? base.getTotalVocKg() : 0.0;

            double gasolineRefuelingVocKg = (DEFAULT_GASOLINE_THROUGHPUT_LITERS * EVAPORATION_RATE_GASOLINE_G_PER_L) / 1000.0;
            double ethanolRefuelingVocKg = (DEFAULT_ETHANOL_THROUGHPUT_LITERS * EVAPORATION_RATE_ETHANOL_G_PER_L) / 1000.0;
            double totalRefuelingVocKg = gasolineRefuelingVocKg + ethanolRefuelingVocKg;

            double calculatedTotalVocKg = baselineVocKg + totalRefuelingVocKg;
            double deltaVocKg = totalRefuelingVocKg;
            double deltaPercentage = (baselineVocKg > 0) ? (deltaVocKg / baselineVocKg) * 100.0 : 0.0;

            ScientificModelResult result = ScientificModelResult.builder()
                    .gasStationId(station.getId())
                    .modelName(MODEL_NAME)
                    .calculationTimestamp(now)
                    .temperatureCelsius(tempC)
                    .gasolineThroughputLiters(DEFAULT_GASOLINE_THROUGHPUT_LITERS)
                    .ethanolThroughputLiters(DEFAULT_ETHANOL_THROUGHPUT_LITERS)
                    .baselineVocKg(baselineVocKg)
                    .refuelingVocKg(totalRefuelingVocKg)
                    .calculatedTotalVocKg(calculatedTotalVocKg)
                    .deltaVocKg(deltaVocKg)
                    .deltaPercentage(deltaPercentage)
                    .build();

            results.add(result);
        }

        return results;
    }
}

```

### 4.3. Entidade de Persistência (`ScientificModelResult.java`)

```java
package com.example.emissionsCalculator.scientific.vocGasStation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scientific_model_results", indexes = {
        @Index(name = "idx_scientific_station_model", columnList = "gas_station_id, model_name, calculation_timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScientificModelResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_station_id", nullable = false)
    private Long gasStationId;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "calculation_timestamp", nullable = false)
    private LocalDateTime calculationTimestamp;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Column(name = "gasoline_throughput_liters")
    private Double gasolineThroughputLiters;

    @Column(name = "ethanol_throughput_liters")
    private Double ethanolThroughputLiters;

    @Column(name = "baseline_voc_kg", nullable = false)
    private Double baselineVocKg;

    @Column(name = "refueling_voc_kg", nullable = false)
    private Double refuelingVocKg;

    @Column(name = "calculated_total_voc_kg", nullable = false)
    private Double calculatedTotalVocKg;

    @Column(name = "delta_voc_kg", nullable = false)
    private Double deltaVocKg;

    @Column(name = "delta_percentage", nullable = false)
    private Double deltaPercentage;
}

```

### 4.4. Camada de Repositório (`ScientificModelResultRepository.java`)

```java
package com.example.emissionsCalculator.scientific.vocGasStation.repository;

import com.example.emissionsCalculator.scientific.vocGasStation.entity.ScientificModelResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScientificModelResultRepository extends JpaRepository<ScientificModelResult, Long> {

    List<ScientificModelResult> findByModelNameOrderByCalculationTimestampDesc(String modelName);

    List<ScientificModelResult> findByGasStationIdOrderByCalculationTimestampDesc(Long gasStationId);
}

```

### 4.5. Motor Orquestrador do Framework (`StationEmissionsEngine.java`)

```java
package com.example.emissionsCalculator.scientific.vocGasStation.engine;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.scientific.vocGasStation.entity.ScientificModelResult;
import com.example.emissionsCalculator.scientific.vocGasStation.repository.ScientificModelResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationEmissionsEngine {

    private final List<EmissionModelStrategy> activeStrategies;
    private final ScientificModelResultRepository resultRepository;

    public void runExperimentalModels(
            List<CmaGasStation> stations,
            HourlyWeatherRecord weather,
            Map<Long, GasStationEmission> baselineMap
    ) {
        if (activeStrategies.isEmpty()) {
            log.info("ℹ️ Nenhum modelo científico experimental habilitado no ambiente.");
            return;
        }

        log.info("🧪 Executando {} modelo(s) científico(s) ativo(s)...", activeStrategies.size());

        List<GasStationEmission> baselineList = new ArrayList<>(baselineMap.values());
        List<ScientificModelResult> resultsToSave = new ArrayList<>();

        for (EmissionModelStrategy strategy : activeStrategies) {
            log.info("▶️ Processando estratégia: [{}]", strategy.getModelName());
            List<ScientificModelResult> modelResults = strategy.calculate(stations, weather, baselineList);
            resultsToSave.addAll(modelResults);
        }

        if (!resultsToSave.isEmpty()) {
            resultRepository.saveAll(resultsToSave);
            log.info("✅ Total de {} resultados salvos na tabela 'scientific_model_results'!", resultsToSave.size());
        }
    }
}

```

### 4.6. Camada de Controle REST (`ScientificModelController.java`)

```java
package com.example.emissionsCalculator.scientific.vocGasStation.controller;

import com.example.emissionsCalculator.scientific.vocGasStation.entity.ScientificModelResult;
import com.example.emissionsCalculator.scientific.vocGasStation.repository.ScientificModelResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scientific-models")
@RequiredArgsConstructor
public class ScientificModelController {

    private final ScientificModelResultRepository repository;

    @GetMapping("/results/{modelName}")
    public ResponseEntity<List<ScientificModelResult>> getResultsByModel(@PathVariable String modelName) {
        return ResponseEntity.ok(repository.findByModelNameOrderByCalculationTimestampDesc(modelName));
    }

    @GetMapping("/results/station/{stationId}")
    public ResponseEntity<List<ScientificModelResult>> getResultsByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(repository.findByGasStationIdOrderByCalculationTimestampDesc(stationId));
    }
}

```

---

## 5. Endpoints REST e Guia de Execução



### 5.1. Executar Simulações e Consultar Resultados



* **Via Swagger UI:** Acesse `http://localhost:8081/swagger-ui/index.html` $\rightarrow$ `scientific-model-controller`.


* **Consultar Resultados do Modelo CETESB 2015 via cURL:**

```bash
curl -X GET "http://localhost:8081/api/scientific-models/results/CETESB_2015_REFUELING"

```



---

## 6. Consultas SQL Avançadas de Validação, Auditoria e Análise Espacial



### 6.1. Consolidação de Emissões Experimentais por Borough de Londres (PostGIS Join)



```sql
SELECT 
    b.name AS borough_name,
    sm.model_name,
    COUNT(sm.id) AS total_postos_simulados,
    ROUND(AVG(sm.baseline_voc_kg)::numeric, 4) AS media_base_epa_kg,
    ROUND(AVG(sm.refueling_voc_kg)::numeric, 4) AS media_refueling_cetesb_kg,
    ROUND(AVG(sm.calculated_total_voc_kg)::numeric, 4) AS media_total_recalculado_kg,
    ROUND(AVG(sm.delta_percentage)::numeric, 2) || '%' AS aumento_medio_percentual
FROM scientific_model_results sm
JOIN cma_gas_stations c ON sm.gas_station_id = c.id
JOIN london_boroughs b ON ST_Contains(b.geometry, ST_SetSRID(ST_MakePoint(c.longitude, c.latitude), 4326))
WHERE sm.calculation_timestamp = (SELECT MAX(calculation_timestamp) FROM scientific_model_results)
GROUP BY b.name, sm.model_name
ORDER BY media_total_recalculado_kg DESC;

```

---

## 7. Referências Bibliográficas e Documentos Recomendados



### 7.1. Referências Citadas



* **CETESB (2015).** *Metodologia de inventário de evaporação de combustível no abastecimento de veículos leves do ciclo Otto*. Companhia Ambiental do Estado de São Paulo, São Paulo, SP.


* **SZWARC, A. et al. (2014).** *Redução da emissão evaporativa do veículo em movimento e no reabastecimento de combustível*. XXI Simpósio Internacional de Engenharia Automotiva (AEA).


* **U.S. EPA (2008).** *AP-42: Compilation of Air Pollutant Emission Factors - Volume I: Stationary Point and Area Sources*, 5ª Edição.