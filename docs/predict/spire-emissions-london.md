# 🌍 S.P.I.R.E. Emissions Engine & Predictive Platform (London)

Plataforma avançada de engenharia analítica, monitorização geoespacial e simulação preditiva de emissões evaporativas e poluentes atmosféricos para a Grande Londres.

---

## 🏛️ 1. Arquitetura Geral do Sistema
O ecossistema foi construído sobre uma arquitetura modular em **Spring Boot**, integrando fontes de dados governamentais e de código aberto em tempo real com motores de cálculo científico avançados.

* **Motor Científico MVEL (`DynamicScientificEmissionModelStrategy`)**: Avalia regras físico-químicas e regulatórias importadas de ficheiros científicos (CSV/PDF) com segurança estrita contra injeção de código hostil.
* **Estimador Dinâmico de Tráfego (`StationThroughputEstimator`)**: Calcula o volume diário e a divisão de combustíveis (E10, Etanol, Diesel) por posto com base em critérios reais da CMA (supermercados, autoestradas, grandes marcas e densidade por *postcode*).
* **Monitorização Geoespacial e Hotspots (`SpatialHotspotService`)**: Cruza dados de postos e estações da LAQN utilizando interpolação espacial IDW (Inverse Distance Weighting) e modelos de plumas gaussianas (Pasquill-Gifford) para deteção automática de alertas de severidade (`CRITICAL`, `WARNING`, `DANGER`).

---

## 🔮 2. Módulo de Simulação Preditiva "What-If Scenarios"
O novo endpoint avançado permite que analistas e decisores políticos simulem o impacto futuro de cenários meteorológicos e de tráfego extremos.

### Endpoint HTTP
* **URL:** `POST /api/predict/what-if`
* **Content-Type:** `application/json`

### Exemplo de Payload (Request)
```json
{
  "borough": "Westminster",
  "throughputMultiplier": 1.30,
  "temperatureCelsius": 35.0,
  "windSpeedKmh": 6.5,
  "relativeHumidity": 40.0,
  "simulationTimestamp": "2026-10-15T16:00:00"
}