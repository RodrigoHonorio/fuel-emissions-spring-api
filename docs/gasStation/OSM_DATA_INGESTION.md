# Documentação Técnica e Científica: Ingestão de Dados Geográfico-Operacionais via OpenStreetMap (Overpass API)

---

## 1. Visão Geral para Pesquisadores e Analistas (Sem Necessidade de Programação)

### 1.1. Contexto e Origem dos Dados
Os dados de postos de combustível georreferenciados utilizam como fonte primária o **OpenStreetMap (OSM)**, um ecossistema global colaborativo de dados geográficos abertos mantido pela OpenStreetMap Foundation. 

No âmbito da ciência de dados urbanos, análise espacial e geografia quantitativa, o OSM é amplamente reconhecido pela riqueza de Metadados Ponto de Interesse (POI - *Point of Interest*).

### 1.2. Definição do Escopo Espacial e Taxonomia
* **Sistema de Referência Geodésica:** WGS 84 (EPSG:4326), padrão internacional para coordenadas globais de Latitude e Longitude em graus decimais.
* **Filtro Taxonômico (Key-Value):** `amenity = fuel`. Na ontologia do OSM, esta chave classifica instalações públicas ou privadas destinadas ao reabastecimento de veículos automotores.
* **Delimitação Territorial:** Região Metropolitana de Londres (*Greater London* / Código de divisão administrativa GB-ENG).

### 1.3. Atributos Coletados e Importância Analítica
| Atributo / Campo | Descrição Científica / Domínio | Aplicação em Pesquisa |
| :--- | :--- | :--- |
| **Node ID (`osm_id`)** | Identificador único universal do nó no grafo global do OpenStreetMap. | Garante rastreabilidade, auditabilidade e joins espaciais. |
| **Latitude & Longitude** | Coordenadas geográficas precisas do ponto central do posto. | Análises de densidade espacial, acessibilidade e buffer de cobertura. |
| **Name / Brand** | Razão social ou bandeira comercial (ex: Shell, BP, Esso). | Análise de concorrência espacial e participação de mercado. |
| **Street / Postcode** | Endereço postal completo e código de endereçamento britânico (UK Postcode). | Agrupamento por setores censitários e áreas socioeconômicas. |
| **AdBlue / Serviços** | Presença de bombas ou galões de AdBlue (Arla 32), calibradores de pneu, etc. | Estudos de logística de transporte de carga pesada e infraestrutura verde. |

---

## 2. Visão Técnica para Engenheiros de Software e Desenvolvedores

### 2.1. Arquitetura da Solução de Ingestão
A ingestão realiza uma requisição HTTP REST síncrona para a **Overpass API** (serviço de consulta de dados brutos do OSM via linguagem Overpass QL).

```
[ Overpass API Server ] 
         │ (HTTP POST/GET com Overpass QL)
         ▼
[ OverpassResponseDto ] ──── (Mapeamento Jackson / JSON)
         │
         ▼
[ OsmGasStationIngestionService ] ──── (Conversão de DTO para Entidade)
         │
         ▼
[ GasStation (JPA Entity) ] ──── (Persistência via Spring Data JPA)
         │
         ▼
[ Banco de Dados PostgreSQL ]
```

### 2.2. Linguagem de Consulta: Overpass QL
A busca utiliza a seguinte estrutura Overpass QL codificada na requisição:

```query
[out:json][timeout:25];
area["ISO3166-2"="GB-ENG"]["name"="Greater London"]->.searchArea;
(
  node["amenity"="fuel"](area.searchArea);
  way["amenity"="fuel"](area.searchArea);
);
out body;
>;
out skel qt;
```

### 2.3. Mapeamento de Classes e Componentes no Projeto
1. **DTO (`OverpassResponseDto.java`):** Captura a estrutura JSON hierárquica retornada pela Overpass API (`elements[]`, `tags`, `lat`, `lon`).
2. **Entidade (`GasStation.java`):** Mapeia a tabela `gas_stations` no PostgreSQL utilizando anotações JPA (`@Entity`, `@Table`, `@Id`).
3. **Serviço (`OsmGasStationIngestionService.java`):** 
   * Instancia a chamada via `RestTemplate` ou `WebClient`.
   * Percorre a lista de `elements`.
   * Realiza a higienização (*parsing*) de strings e prevenção de duplicatas (`upsert` ou verificação por `osm_id`).
4. **Controlador (`OsmGasStationIngestionController.java`):** 
   * Expõe o endpoint HTTP `POST /api/gas-stations/sync`.
   * Dispara a sincronização sob demanda.

---

## 3. Instruções de Execução e Teste

### Como Executar o Endpoint
* **Via Swagger UI:** Aceder a `http://localhost:8081/swagger-ui/index.html` $ightarrow$ `osm-gas-station-ingestion-controller` $ightarrow$ `POST /api/gas-stations/sync` $ightarrow$ **Execute**.
* **Via cURL:**
  ```bash
  curl -X POST "http://localhost:8081/api/gas-stations/sync"
  ```
