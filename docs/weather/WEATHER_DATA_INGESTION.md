# Documentação Técnica e Científica: Ingestão de Dados Meteorológicos e Séries Temporais Climáticas (Open-Meteo API)

---

## 1. Visão Geral para Pesquisadores e Analistas Ambientais (Sem Necessidade de Programação)

### 1.1. Contexto e Origem dos Dados
A modelagem de emissões atmosféricas de **Compostos Orgânicos Voláteis (VOCs)** em postos de reabastecimento exige dados microclimáticos de alta resolução temporal. Os dados de previsão e histórico meteorológico são obtidos via **Open-Meteo API**, que integra modelos numéricos globais de previsão do tempo de alta precisão, tais como:
* **ECMWF (European Centre for Medium-Range Weather Forecasts):** Modelo IFS de alta resolução espacial.
* **GFS / NOAA (National Oceanic and Atmospheric Administration - EUA):** Modelo global de previsão numérica de alta variabilidade.
* **ERA5 Reanalysis:** Reanálise climática do Copernicus Climate Change Service para dados históricos retroativos.

### 1.2. Relevância Científica na Química de Emissões Fugidias
Na engenharia ambiental e físico-química de hidrocarbonetos, a temperatura do ar e o microclima têm papel direto no fenômeno de evaporação de combustíveis:
* **Pressão de Vapor Reid (RVP):** A volatilidade de compostos aromáticos (como benzeno, tolueno, xileno) aumenta exponencialmente com o aumento da temperatura ambiente ($T$).
* **Evasão durante o Reabastecimento (*Refueling Losses*):** O aumento da temperatura expande a fase de vapor dentro dos tanques subterrâneos e nos tanques dos veículos, forçando a saída de gases para a atmosfera durante a operação de bombeamento.
* **Fatores Secundários (Vento e Umidade):** A velocidade do vento ($km/h$) influencia a dispersão de poluentes na camada limite urbana, enquanto a umidade e a precipitação afetam o tempo de permanência atmosférica de compostos e o fluxo de veículos no posto.

### 1.3. Dicionário de Dados e Variáveis da Série Temporal
A tabela **`hourly_weather_records`** armazena os dados estruturados numa **série temporal horária contínua** para a Região Metropolitana de Londres (*Greater London*):

| Campo na Base de Dados | Variável Físico-Química / Meteorológica | Unidade de Medida | Importância para a Pesquisa de Emissões |
| :--- | :--- | :--- | :--- |
| `location_name` | Identificador geográfico de referência | Texto (ex: `Greater London`) | Delimitação da célula de amostragem climática. |
| `forecast_timestamp` | Timestamp exato da medição/previsão | ISO 8601 (`YYYY-MM-DDTHH:MM`) | Marca temporal para correlação com o volume de vendas/hora. |
| `temperature_celsius` | Temperatura do ar a 2 metros de altura | Graus Celsius (°C) | **Variável Primária:** Determina a taxa de volatilidade e emissão de VOCs. |
| `apparent_temperature_celsius` | Sensação térmica percebida | Graus Celsius (°C) | Indicador de desconforto térmico e variação comportamental. |
| `relative_humidity_percentage` | Umidade relativa do ar a 2 metros | Percentual (%) | Modula reações fotoquímicas de formação de Ozônio ($O_3$) troposférico. |
| `precipitation_mm` | Volume de precipitação pluviométrica | Milímetros (mm) | Correlaciona-se com variações na demanda e tráfego de veículos. |
| `wind_speed_kmh` | Velocidade do vento a 10 metros de altura | Quilômetros por hora (km/h) | Define a taxa de diluição e taxa de dispersão local de poluentes. |
| `updated_at` | Carimbo de data/hora da sincronização | ISO 8601 (`Timestamp`) | Garante auditabilidade da versão do dado coletado. |

---

## 2. Visão Técnica para Engenheiros de Software e Desenvolvedores

### 2.1. Arquitetura da Solução e Fluxo de Dados
A ingestão consome a API REST pública do Open-Meteo de forma síncrona, desserializa a estrutura JSON contendo arrays paralelos e realiza um procedimento de **Upsert** (preservação idempotente) no banco de dados PostgreSQL.

```
[ Open-Meteo REST API ]
         │ (HTTP GET - JSON com séries temporais de 384h)
         ▼
[ OpenMeteoResponseDto ] ──────── (Desserialização Jackson de Arrays Paralelos)
         │
         ▼
[ WeatherIngestionService ] ───── (Mapeamento / Parsing temporal ISO-8601)
         │
         ▼
[ HourlyWeatherRecord (JPA) ] ── (Padrão Upsert por Location + Timestamp)
         │
         ▼
[ PostgreSQL: hourly_weather_records ]
```

### 2.2. Parâmetros do Endpoint Externo (Open-Meteo API)
* **URL de Consulta:** `https://api.open-meteo.com/v1/forecast`
* **Parâmetros Geográficos:** `latitude=51.5074&longitude=-0.1278` (Centro de Londres).
* **Métricas Solicitadas (`hourly`):** `temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,wind_speed_10m`
* **Horizonte Temporal:** `forecast_days=16` (Gera 384 registos horários futuros por execução).

### 2.3. Mapeamento de Componentes de Software
1. **DTO (`OpenMeteoResponseDto.java`):**
   * Estrutura interna `HourlyData` mapeada com anotações `@JsonProperty` para descompactar as listas de cada métrica.
2. **Entidade (`HourlyWeatherRecord.java`):**
   * Mapeia a tabela `hourly_weather_records`.
   * Possui restrição de unicidade composta (`@UniqueConstraint`) nas colunas `(location_name, forecast_timestamp)`.
3. **Repositório (`HourlyWeatherRecordRepository.java`):**
   * Declara o método de consulta customizado `findByLocationNameAndForecastTimestamp()`.
4. **Serviço (`WeatherIngestionService.java`):**
   * Executa a requisição HTTP via `RestClient`.
   * Percorre a série temporal criando ou atualizando instâncias existentes para prevenir duplicação de dados.
5. **Controlador (`WeatherIngestionController.java`):**
   * Expõe a rota HTTP `POST /api/weather-forecast/sync`.

---

## 3. Estratégia de Séries Temporais, Histórico e Treinamento de IA

### 3.1. Arquitetura de Tabela Única (*Time-Series Table*)
Ao invés de dividir o sistema entre "Clima Atual" e "Previsão", utiliza-se o padrão de **Série Temporal Unificada**:
* À medida que o tempo cronológico avança, os dados gravados como "previsão" convertem-se automaticamente no **histórico meteorológico consolidado** da aplicação.
* Este modelo simplifica as consultas SQL para treinamento de modelos de Machine Learning (ex: Regressão Linear, XGBoost, Redes Neurais LSTM/Transformers) para cálculo preditivo de emissões.

### 3.2. Carga Retroativa de Histórico (*Historical Backfill*)
Caso a equipe de pesquisa necessite de histórico meteorológico de anos anteriores (ex: 2020 a 2025) para calibrar modelos de IA imediatamente:
* A Open-Meteo fornece a **Historical Weather API** (`https://archive-api.open-meteo.com/v1/archive`).
* O mesmo DTO e a mesma Entidade JPA podem ser reutilizados para importar dados retroativos de múltiplos anos sem alterar o esquema da base de dados.

---

## 4. Instruções de Execução, Validação e Teste

### 4.1. Como Executar a Ingestão
* **Via Swagger UI:** Acesse `http://localhost:8081/swagger-ui/index.html` $ightarrow$ `weather-ingestion-controller` $ightarrow$ `POST /api/weather-forecast/sync` $ightarrow$ **Execute**.
* **Via cURL:**
  ```bash
  curl -X POST "http://localhost:8081/api/weather-forecast/sync"
  ```

### 4.2. Validação SQL na Base de Dados
Para confirmar a integridade e contagem total de horas importadas (esperado: 384 registos para 16 dias):

```sql
-- Verificar a contagem total de registos horários
SELECT COUNT(*) FROM hourly_weather_records;

-- Consultar a série temporal em ordem cronológica
SELECT location_name, forecast_timestamp, temperature_celsius, relative_humidity_percentage, wind_speed_kmh
FROM hourly_weather_records
ORDER BY forecast_timestamp ASC;
```
