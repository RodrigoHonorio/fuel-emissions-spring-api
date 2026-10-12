Documentação Técnica: Framework Extensível de Modelos Científicos de Emissões (SCIENTIFIC_MODELS_FRAMEWORK.md)1. Visão Geral e Princípios de ArquiteturaO Framework Extensível de Modelos Científicos de Emissões foi desenvolvido para permitir a integração, execução paralela e comparação de múltiplos artigos e metodologias científicas de estimativa de Compostos Orgânicos Voláteis (VOCs) em postos de combustível, sem comprometer as entidades ou tabelas de domínio principal da aplicação.Princípios Fundamentais de DesignStrategy Pattern: Abstração completa do cálculo através da interface EmissionModelStrategy.Non-Invasive Persistence (Isolamento de Domínio): As tabelas oficiais do sistema (cma_gas_stations e gas_station_emissions) mantêm-se como fontes da verdade de leitura. Todos os resultados de simulações, deltas e variações são gravados exclusivamente na tabela dedicada scientific_model_results.Dynamic Orchestration Engine: O serviço StationEmissionsEngine descobre automaticamente todas as implementações ativas no contexto Spring e as executa em lote.Feature Toggles via Configuration: Cada modelo científico possui governança individual via application.properties utilizando a anotação @ConditionalOnProperty, permitindo ativar ou desativar simulações sem alteração ou recompilação de código.2. Estrutura de Pacotes e ComponentesPlaintextsrc/main/java/com/example/teste/
└── scientific/
└── vocGasStation/
├── engine/
│   ├── EmissionModelStrategy.java
│   └── StationEmissionsEngine.java
├── entity/
│   └── ScientificModelResult.java
├── repository/
│   └── ScientificModelResultRepository.java
└── models/
└── CetesbEmissionModel.java
3. Modelo de Dados (Tabela scientific_model_results)A tabela foi projetada para atuar como um repositório central de séries temporais de simulações experimentais.ColunaTipo SQLDescriçãoidBIGINT (PK)Chave primária autoincrementável do registro experimental.gas_station_idBIGINTIdentificador do posto de combustível da CMA analisado.model_nameVARCHAR(100)Nome/código do modelo científico (ex: CETESB_2015_REFUELING).calculation_timestampTIMESTAMPCarimbo de data e hora em que a simulação foi executada.temperature_celsiusDOUBLE PRECISIONTemperatura ambiente (°C) registrada no momento do cálculo.gasoline_throughput_litersDOUBLE PRECISIONVolume diário de gasolina considerado no modelo ($L$).ethanol_throughput_litersDOUBLE PRECISIONVolume diário de etanol considerado no modelo ($L$).baseline_voc_kgDOUBLE PRECISIONEmissão de VOCs da base oficial de referência EPA AP-42 ($kg$).refueling_voc_kgDOUBLE PRECISIONEmissão adicional de VOCs calculada pelo artigo científico ($kg$).calculated_total_voc_kgDOUBLE PRECISIONEmissão total reestimada pelo modelo ($Baseline + Refueling$).delta_voc_kgDOUBLE PRECISIONDiferença absoluta em massa de VOCs em relação à base ($kg$).delta_percentageDOUBLE PRECISIONVariação percentual ($Status\ \% = \frac{Delta}{Baseline} \times 100$).4. Código-Fonte Completo da Infraestrutura4.1. Interface Strategy (EmissionModelStrategy.java)Javapackage com.example.emissionsCalculator.scientific.vocGasStation.engine;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.scientific.vocGasStation.entity.ScientificModelResult;

import java.util.List;

/**
* <b>O que é:</b> Contrato universal para estratégias científicas de cálculo de emissões.
* <b>Para que serve:</b> Padronizar a assinatura de entrada e saída para qualquer artigo científico integrado.
  */
  public interface EmissionModelStrategy {

  /**
    * Retorna o nome identificador do modelo científico.
      */
      String getModelName();

  /**
    * Executa o cálculo experimental para uma coleção de postos de combustível.
      */
      List<ScientificModelResult> calculate(
      List<CmaGasStation> stations,
      HourlyWeatherRecord weather,
      List<GasStationEmission> baselineEmissions
      );
      }
      4.2. Entidade de Persistência (ScientificModelResult.java)Javapackage com.example.emissionsCalculator.scientific.vocGasStation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
* <b>O que é:</b> Entidade JPA para armazenamento dos resultados experimentais.
* <b>Para que serve:</b> Gravar os cálculos de modelos científicos em uma estrutura dedicada sem alterar o domínio principal.
  */
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
  4.3. Repositório JPA (ScientificModelResultRepository.java)Javapackage com.example.emissionsCalculator.scientific.vocGasStation.repository;

import com.example.emissionsCalculator.scientific.vocGasStation.entity.ScientificModelResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScientificModelResultRepository extends JpaRepository<ScientificModelResult, Long> {

    List<ScientificModelResult> findByModelNameOrderByCalculationTimestampDesc(String modelName);

    List<ScientificModelResult> findByGasStationIdOrderByCalculationTimestampDesc(Long gasStationId);
}
4.4. Orquestrador do Framework (StationEmissionsEngine.java)Javapackage com.example.emissionsCalculator.scientific.vocGasStation.engine;

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

/**
* <b>O que é:</b> Motor orquestrador do framework de modelos científicos.
* <b>Para que serve:</b> Injetar dinamicamente todas as estratégias ativas e gerenciar a execução isolada em lote.
  */
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
5. Governança e Configuração (application.properties)A ativação de cada modelo é gerenciada de forma declarativa:Properties# Habilita o modelo da CETESB (2015) para cálculo de perdas no abastecimento
   scientific.model.cetesb2015.enabled=true

# Exemplo para novos modelos futuros:
# scientific.model.european_stage2.enabled=false
6. Consultas SQL de Auditoria e AnáliseAgrupamento por Lote de SimulaçãoSQLSELECT
   calculation_timestamp,
   model_name,
   COUNT(*) AS total_postos_processados
   FROM scientific_model_results
   GROUP BY calculation_timestamp, model_name
   ORDER BY calculation_timestamp DESC;
   Consulta da Última Simulação ExecutadaSQLSELECT
   gas_station_id,
   model_name,
   baseline_voc_kg AS base_epa_kg,
   refueling_voc_kg AS acrescimo_abastecimento_kg,
   calculated_total_voc_kg AS total_cetesb_kg,
   ROUND(delta_percentage::numeric, 2) || '%' AS aumento_percentual,
   calculation_timestamp
   FROM scientific_model_results
   WHERE calculation_timestamp = (SELECT MAX(calculation_timestamp) FROM scientific_model_results)
   ORDER BY gas_station_id ASC;