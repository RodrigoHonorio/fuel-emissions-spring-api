# Documentação Técnica e Científica: Framework S.P.I.R.E. e Modelo Experimental de Emissões de Abastecimento (`SPIRE_EMISSIONS_FRAMEWORK_V2.md`)

---

## 1\. Visão Geral para Pesquisadores e Analistas Ambientais

### 1.1. Contexto e Relevância Científica

Nos inventários tradicionais de emissões atmosféricas urbanas (como a metodologia padrão baseada na norma EPA AP-42), contabilizam-se prioritariamente as emissões de escapamento e as perdas evaporativas do sistema de combustível do veículo em repouso ou movimento (*Diurnal & Hot Soak Losses*).

No entanto, o processo de transferência de combustível da bomba do posto para o tanque do veículo gera **perdas por deslocamento de vapor** (*Refueling Losses*). À medida que o combustível líquido entra no reservatório, o ar saturado de vapores de Compostos Orgânicos Voláteis (VOCs) acumulado no interior do tanque é expulso diretamente para a atmosfera.

O módulo **S.P.I.R.E. (System for Pollution & Integrated Refueling Emissions)** integra a metodologia experimental desenvolvida pela **CETESB (Companhia Ambiental do Estado de São Paulo, 2015\)** ao ecossistema georreferenciado da Região Metropolitana de Londres. O objetivo é reestimar e auditar o verdadeiro impacto dos postos de combustível na qualidade do ar e na formação de ozônio troposférico (\$O\_3\$).

### 1.2. Dicionário de Atributos do Modelo Experimental (`scientific_model_results`)

| Atributo / Coluna SQL | Definição Científica / Operacional | Tipo / Unidade |
| :---- | :---- | :---- |
| `id` | Chave primária da tabela de resultados experimentais. | `BIGSERIAL` |
| `gas_station_id` | Identificador único do posto de combustível analisado na base da CMA/OSM. | `BIGINT` |
| `model_name` | Código identificador da estratégia científica executada (ex: `CETESB_2015_REFUELING`). | `VARCHAR` |
| `calculation_timestamp` | Momento exato da simulação e persistência horária. | `TIMESTAMP` |
| `temperature_celsius` | Temperatura do ar no momento do cálculo obtida da estação meteorológica mais próxima. | `°C` (`DOUBLE`) |
| `gasoline_throughput_liters` | Volume diário estimado/alocado de comercialização de gasolina. | Litros (`DOUBLE`) |
| `ethanol_throughput_liters` | Volume diário estimado/alocado de comercialização de etanol. | Litros (`DOUBLE`) |
| `baseline_voc_kg` | Emissão de referência diária calculada pelo modelo padrão EPA AP-42. | `kg/dia` |
| `refueling_voc_kg` | Acréscimo de massa de VOCs gerado exclusivamente pela operação de abastecimento. | `kg/dia` |
| `calculated_total_voc_kg` | Emissão total reestimada (\$\\text{Calculated} \= \\text{Baseline} \+ \\text{Refueling}\$). | `kg/dia` |
| `delta_voc_kg` | Variação absoluta de emissão gerada pelo modelo científico. | `kg/dia` |
| `delta_percentage` | Variação percentual de aumento na estimativa (\$\\Delta\_{%}\$). | `%` |

---

## 2\. Fundamentação Científica e Modelagem Matemática

### 2.1. Equação Fundamental da CETESB (2015)

A massa de vapor emitida durante a operação de reabastecimento veicular (\$T\_g\$) é calculada pela relação linear entre o volume de combustível transferido e a taxa de evaporação específica levantada experimentalmente em câmara SHED:

\$\$T\_g \= C \\cdot C\_e\$\$

Onde:

- \$T\_g\$: Taxa total de emissão de vapor gerado no abastecimento (gramas, \$g\$).  
- \$C\$: Volume de combustível transferido durante a operação (litros, \$L\$).  
- \$C\_e\$: Taxa de evaporação experimental específica (\$g/L\$).

### 2.2. Fatores de Evaporação Experimentais (\$C\_e\$)

- **Gasolina C:** \$1{,}14 \\text{ g/L}\$ (mistura comercial com etanol anidro).  
- **Etanol Hidratado:** \$0{,}37 \\text{ g/L}\$ (etanol combustível comercial).

### 2.3. Formulação dos Deltas e Comparação com a Base Neutra EPA AP-42

\$\$\\Delta\_{\\text{VOC}} \= \\text{Refueling}*{\\text{VOC}} \= \\frac{(C*{\\text{gasolina}} \\cdot C\_{e,\\text{gasolina}}) \+ (C\_{\\text{etanol}} \\cdot C\_{e,\\text{etanol}})}{1000}\$\$ \$\$\\Delta\_{%} \= \\left( \\frac{\\Delta\_{\\text{VOC}}}{\\text{Baseline}\_{\\text{VOC}}} \\right) \\times 100\$\$

---

## 3\. Arquitetura de Software e Orquestração do Motor

### 3.1. Visão Geral dos Componentes Atualizados

- **`StationEmissionsEngine`**: Orquestrador responsável por recolher os dados mais recentes da base de dados (`CmaGasStation`, `HourlyWeatherRecord`, `GasStationEmission`) através do método `executeCurrentHourModels()` e delegar a execução para as estratégias ativas (`EmissionModelStrategy`).  
- **`CetesbEmissionModel`**: Implementação concreta ativada via feature toggle (`scientific.models.cetesb.enabled=true`).  
- **Persistência Isolada (`ScientificModelResultRepository`)**: Garante que o núcleo oficial de tabelas permaneça inalterado, gravando simulações na tabela `scientific_model_results` (atualmente com 550+ registros consolidados).

---

## 4\. Estado Atual das Bases de Dados (Verificação em Produção/Testes)

- **London Boundaries:** 33 distritos oficiais cadastrados.  
- **CMA Gas Stations:** 275 postos oficiais monitorados em Londres.  
- **Hourly Weather Records:** 384 registros meteorológicos horários sincronizados via Open-Meteo.  
- **Air Quality Stations:** 256 estações de monitoramento.  
- **Scientific Model Results:** 550 simulações calculadas (`CETESB_2015_REFUELING` com média de \~6.77 kg VOC).

---

## 5\. Consultas SQL de Auditoria e Validação Espacial

### 5.1. Consolidação de Emissões Experimentais por Borough (PostGIS)

SELECT 

    b.borough\_name AS borough\_name,

    sm.model\_name,

    COUNT(sm.id) AS total\_postos\_simulados,

    ROUND(AVG(sm.baseline\_voc\_kg)::numeric, 4\) AS media\_base\_epa\_kg,

    ROUND(AVG(sm.refueling\_voc\_kg)::numeric, 4\) AS media\_refueling\_cetesb\_kg,

    ROUND(AVG(sm.calculated\_total\_voc\_kg)::numeric, 4\) AS media\_total\_recalculado\_kg,

    ROUND(AVG(sm.delta\_percentage)::numeric, 2\) || '%' AS aumento\_medio\_percentual

FROM scientific\_model\_results sm

JOIN cma\_gas\_stations c ON sm.gas\_station\_id \= c.id

JOIN london\_boundaries b ON ST\_Contains(

    ST\_GeomFromGeoJSON(b.geojson\_geometry), 

    ST\_SetSRID(ST\_MakePoint(c.longitude, c.latitude), 4326\)

)

WHERE sm.calculation\_timestamp \= (SELECT MAX(calculation\_timestamp) FROM scientific\_model\_results)

GROUP BY b.borough\_name, sm.model\_name

ORDER BY media\_total\_recalculado\_kg DESC;