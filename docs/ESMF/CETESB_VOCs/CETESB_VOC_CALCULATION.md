1. Visão Geral e Fundamentação Científica
   Este módulo implementa o modelo matemático e os fatores experimentais de emissão publicados no relatório técnico oficial da CETESB (Companhia Ambiental do Estado de São Paulo) de Janeiro de 2015:

Título Oficial: "Metodologia de inventário de evaporação de combustível no abastecimento de veículos leves do ciclo Otto".

Contexto do Estudo
Nos inventários tradicionais de fontes móveis (como o padrão EPA AP-42), contabilizam-se prioritariamente as emissões de escapamento e as perdas evaporativas ocorridas dentro do próprio veículo em repouso ou movimento. No entanto, o processo de transferência de combustível da bomba do posto para o tanque do veículo gera perdas por deslocamento de vapor (Refueling Losses), nas quais o líquido entrante força a saída dos vapores de combustível acumulados no tanque diretamente para a atmosfera.

O modelo quantifica o acréscimo de Compostos Orgânicos Voláteis (COV) e Hidrocarbonetos Não Metano (NMHC) decorrente dessa operação, fornecendo dados cruciais para modelos de química atmosférica e formação de ozônio troposférico (O
3
​
).

2. Modelagem Matemática e Fatores de Emissão
   2.1. Equação Fundamental do Modelo
   A taxa diária ou anual de emissão de vapor gerado no abastecimento (T
   g
   ​
   ) é calculada pela relação linear entre o volume de combustível comercializado e sua respectiva taxa de evaporação experimental:

T
g
​
=C⋅C
e
​

Onde:

T
g
​
: Massa total de vapor de combustível liberada no abastecimento em gramas (g).

C: Volume de combustível transferido durante a operação em litros (L).

C
e
​
: Taxa de evaporação específica do combustível em gramas por litro (g/L).

2.2. Taxas Experimentais de Evaporação (C
e
​
)
Os fatores de emissão foram obtidos pela CETESB através de testes em câmara hermética SHED (Sealed Housing for Evaporative Determination) utilizando detectores de ionização de chamas calibrados:

Combustível Analisado	Taxa de Evaporação (C
e
​
)	Unidade	Descrição do Padrão do Estudo
Gasolina C	1,14	g/L	Gasolina comercial contendo 22% de etanol anidro (Portaria ANP nº 309/2001).
Etanol Hidratado	0,37	g/L	Etanol combustível comercial.
2.3. Impacto Observado no Relatório Técnico da CETESB
A inclusão do cálculo de abastecimento nos inventários veiculares da Região Metropolitana de São Paulo (RMSP) revelou:

+28% de aumento estimado nas emissões de Hidrocarbonetos Não Metano (NMHC).

+22% de aumento no total de COVs lançados na atmosfera por fontes veiculares.

3. Demonstração dos Cálculos Aplicados à Aplicação
   No contexto da aplicação, cada posto oficial da CMA é simulado considerando uma distribuição diária estimada de vendas de 4500 L de Gasolina C e 500 L de Etanol Hidratado.

Passo 1: Cálculo da Emissão de Abastecimento por Posto
Emissão por Gasolina C:

M
gasolina
​
=
1000 g/kg
4500 L×1,14 g/L
​
=5,13 kg
Emissão por Etanol Hidratado:

M
etanol
​
=
1000 g/kg
500 L×0,37 g/L
​
=0,185 kg
Acréscimo Total de Abastecimento (Refueling VOC):

Refueling VOC=5,13 kg+0,185 kg=5,315 kg
Passo 2: Comparação com a Base Neutra (EPA AP-42)
Considerando a emissão de referência gerada pelo modelo EPA a uma temperatura padrão de 15
∘
C:

Base Oficial EPA (Baseline): ≈2,9041 kg/dia

Acréscimo CETESB (Refueling): +5,3150 kg/dia

Total Recalculado: 2,9041+5,3150=8,2191 kg/dia

Variação Percentual (Delta %):

Delta %=(
2,9041
5,3150
​
)×100=+183,02%
4. Código-Fonte Completo da Estratégia (CetesbEmissionModel.java)
   Java
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
* <b>O que é:</b> Estratégia de cálculo baseada no relatório CETESB (2015).
* <b>Para que serve:</b> Quantificar emissões adicionais de VOCs geradas por perdas no abastecimento veicular.
  */
  @Slf4j
  @Component
  @ConditionalOnProperty(name = "scientific.model.cetesb2015.enabled", havingValue = "true")
  public class CetesbEmissionModel implements EmissionModelStrategy {

  public static final String MODEL_NAME = "CETESB_2015_REFUELING";

  // Fatores de evaporação oficiais da Tabela 1 do relatório CETESB (2015) em g/L
  private static final double EVAPORATION_RATE_GASOLINE_G_PER_L = 1.14;
  private static final double EVAPORATION_RATE_ETHANOL_G_PER_L = 0.37;

  // Estimativa de volume diário comercializado em postos urbanos (L)
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

           // 1. Cálculo da massa de vapor emitida por combustível (conversão de g para kg)
           double gasolineRefuelingVocKg = (DEFAULT_GASOLINE_THROUGHPUT_LITERS * EVAPORATION_RATE_GASOLINE_G_PER_L) / 1000.0;
           double ethanolRefuelingVocKg = (DEFAULT_ETHANOL_THROUGHPUT_LITERS * EVAPORATION_RATE_ETHANOL_G_PER_L) / 1000.0;
           double totalRefuelingVocKg = gasolineRefuelingVocKg + ethanolRefuelingVocKg;

           // 2. Apuração do total recalculado e do delta
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
5. Consultas SQL de Validação do Estudo
   Validação dos Resultados do Modelo CETESB na Base
   SQL
   SELECT
   gas_station_id,
   model_name,
   baseline_voc_kg AS base_epa_kg,
   refueling_voc_kg AS acrescimo_cetesb_kg,
   calculated_total_voc_kg AS total_recalculado_kg,
   ROUND(delta_percentage::numeric, 2) || '%' AS delta_pct,
   calculation_timestamp
   FROM scientific_model_results
   WHERE model_name = 'CETESB_2015_REFUELING'
   ORDER BY calculation_timestamp DESC, gas_station_id ASC;
   Média Geral do Impacto de Abastecimento nos Postos
   SQL
   SELECT
   model_name,
   AVG(baseline_voc_kg) AS media_base_epa_kg,
   AVG(refueling_voc_kg) AS media_acrescimo_cetesb_kg,
   AVG(calculated_total_voc_kg) AS media_total_kg,
   AVG(delta_percentage) AS media_aumento_percentual
   FROM scientific_model_results
   WHERE model_name = 'CETESB_2015_REFUELING'
   GROUP BY model_name;