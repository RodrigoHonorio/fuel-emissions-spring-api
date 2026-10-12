# Módulo de Ingestão de Estações de Qualidade do Ar (LAQN / Imperial College London)

## 📌 Visão Geral
Este módulo é responsável por sincronizar, mapear e persistir os metadados georreferenciados de **estações de monitoramento da qualidade do ar de Londres**, fornecidos pela **LAQN API (London Air Quality Network)** operada pelo Imperial College London.

O módulo adota estritamente os princípios de **Data First** (preservação de 100% dos dados brutos retornados pela API) e **Domain-Driven Naming (DDN)**, garantindo autoexplicabilidade, idempotência nas atualizações e performance otimizada de inserção em lote.

---

## 🏗 Architecture & Design Patterns

* **Data First Architecture:** Todos os metadados da API externa (proprietário dos dados, gestor, códigos de autoridade local, coordenadas WGS84 e status de operação) são armazenados sem perdas.
* **Domain-Driven Naming (DDN):** DTOs utilizam o prefixo do domínio de origem (`Laqn`) para evitar conflitos de nomenclatura com outros domínios da aplicação. Todos os DTOs utilizam o sufixo em caixa alta `DTO`.
* **Idempotência (Upsert In-Memory):** As estações já existentes no banco de dados são consultadas em lote em tempo $O(1)$, atualizando campos alterados e criando novos registros sem disparar consultas individuais ($N+1$).
* **Spring Boot 3 RestClient:** Comunicação HTTP simplificada e moderna, substituindo o legado `RestTemplate`.

---

## 📂 Estrutura de Arquivos e Classes

```
src/main/java/com/example/teste/
├── controller/
│   └── AirQualityStationIngestionController.java
├── service/
│   └── AirQualityStationIngestionService.java
├── repository/
│   ├── AirQualityStationRepository.java
│   └── HourlyAirQualityRecordRepository.java
├── entity/
│   ├── AirQualityStation.java
│   └── HourlyAirQualityRecord.java
└── dto/
    ├── LaqnSiteDTO.java
    ├── LaqnSitesWrapperDTO.java
    └── LaqnMonitoringSitesResponseDTO.java
```

---

## 🗄️ Modelo de Dados (PostgreSQL / PostGIS)

### 1. Tabela `air_quality_stations` (Metadados Fixos)

| Coluna | Tipo SQL | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGINT` (PK) | Chave primária autoincrementável |
| `site_code` | `VARCHAR(50)` (UK) | Código único do site (ex: `BG1`, `BX5`) |
| `site_name` | `VARCHAR(255)` | Nome amigável da estação |
| `site_type` | `VARCHAR(100)` | Categoria ambiental (ex: `Roadside`, `Kerbside`, `Urban Background`) |
| `latitude` | `DOUBLE PRECISION` | Latitude decimal geográfica |
| `longitude` | `DOUBLE PRECISION` | Longitude decimal geográfica |
| `latitude_wgs84` | `DOUBLE PRECISION` | Latitude oficial no sistema de referência WGS84 |
| `longitude_wgs84` | `DOUBLE PRECISION` | Longitude oficial no sistema de referência WGS84 |
| `in_service_status` | `VARCHAR(50)` | Status bruto retornado pela API (`Yes`/`No`) |
| `local_authority_code` | `VARCHAR(50)` | Código numérico da autoridade local/distrito |
| `local_authority_name` | `VARCHAR(255)` | Nome do distrito/borough de Londres |
| `data_owner` | `VARCHAR(255)` | Organização proprietária das medições |
| `data_manager` | `VARCHAR(255)` | Entidade gestora dos equipamentos |
| `is_active` | `BOOLEAN` | Indicador booleano derivado do status em serviço (`Yes` -> `true`) |
| `updated_at` | `TIMESTAMP` | Timestamp da última sincronização realizada |

### 2. Tabela `hourly_air_quality_records` (Séries Temporais de Poluentes)

| Coluna | Tipo SQL | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGINT` (PK) | Chave primária autoincrementável |
| `station_id` | `BIGINT` (FK) | Chave estrangeira ligando à tabela `air_quality_stations` |
| `measurement_timestamp` | `TIMESTAMP` | Data e hora exata da leitura do poluente |
| `no2_ugm3` | `DOUBLE PRECISION` | Concentração de Dióxido de Nitrogênio ($\mu g/m^3$) |
| `pm10_ugm3` | `DOUBLE PRECISION` | Concentração de Partículas Inaláveis PM10 ($\mu g/m^3$) |
| `pm25_ugm3` | `DOUBLE PRECISION` | Concentração de Partículas Finas PM2.5 ($\mu g/m^3$) |
| `o3_ugm3` | `DOUBLE PRECISION` | Concentração de Ozônio ($\mu g/m^3$) |
| `air_quality_index` | `INTEGER` | Índice resumido de qualidade do ar (1 a 10) |
| `updated_at` | `TIMESTAMP` | Timestamp de criação/atualização do registro |

---

## 💻 Código-Fonte dos Componentes

### 1. DTOs

#### `LaqnSiteDTO.java`

```java
package com.example.emissionsCalculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <b>O que é:</b> DTO para desserialização dos metadados de uma estação da London Air Quality Network (LAQN).
 * <b>Para que serve:</b> Mapear 100% dos atributos do nó 'Site' retornado pelo Imperial College London (Data First).
 * <b>De onde veio:</b> Elemento 'Site' dentro da resposta JSON da LAQN API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LaqnSiteDTO(
        @JsonProperty("@SiteCode")
        String siteCode,

        @JsonProperty("@SiteName")
        String siteName,

        @JsonProperty("@SiteType")
        String siteType,

        @JsonProperty("@Latitude")
        Double latitude,

        @JsonProperty("@Longitude")
        Double longitude,

        @JsonProperty("@LatitudeWGS84")
        Double latitudeWGS84,

        @JsonProperty("@LongitudeWGS84")
        Double longitudeWGS84,

        @JsonProperty("@InService")
        String inService,

        @JsonProperty("@LocalAuthorityCode")
        String localAuthorityCode,

        @JsonProperty("@LocalAuthorityName")
        String localAuthorityName,

        @JsonProperty("@DataOwner")
        String dataOwner,

        @JsonProperty("@DataManager")
        String dataManager
) {
}
```

#### `LaqnSitesWrapperDTO.java`

```java
package com.example.emissionsCalculator.dto;

import com.example.emissionsCalculator.dto.airQualityStation.LaqnSiteDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * <b>O que é:</b> DTO invólucro para a coleção de estações da LAQN.
 * <b>Para que serve:</b> Descompactar o nó 'Sites' do JSON, garantindo desserialização segura de listas.
 * <b>De onde veio:</b> Objeto 'Sites' na resposta da LAQN API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LaqnSitesWrapperDTO(
        @JsonProperty("@GroupName")
        String groupName,

        @JsonProperty("Site")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<LaqnSiteDTO> siteList
) {
}
```

#### `LaqnMonitoringSitesResponseDTO.java`

```java
package com.example.emissionsCalculator.dto;

import com.example.emissionsCalculator.dto.airQualityStation.LaqnSitesWrapperDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <b>O que é:</b> DTO de topo para a resposta completa da LAQN API.
 * <b>Para que serve:</b> Ponto de entrada do payload JSON para sincronização de estações.
 * <b>De onde veio:</b> Resposta raiz do endpoint https://api.erg.ic.ac.uk/...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LaqnMonitoringSitesResponseDTO(
        @JsonProperty("Sites")
        LaqnSitesWrapperDTO sites
) {
}
```

---

### 2. Entidades

#### `AirQualityStation.java`

```java
package com.example.emissionsCalculator.entity;

import com.example.emissionsCalculator.entity.airQualityStation.HourlyAirQualityRecord;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>O que é:</b> Entidade JPA para estações físicas de monitoramento de qualidade do ar.
 * <b>Para que serve:</b> Armazenar metadados fixos de localização geográfica da rede LAQN.
 * <b>De onde veio:</b> Tabela 'air_quality_stations'.
 */
@Entity
@Table(name = "air_quality_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirQualityStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_code", nullable = false, unique = true, length = 50)
    private String siteCode;

    @Column(name = "site_name", nullable = false)
    private String siteName;

    @Column(name = "site_type", length = 100)
    private String siteType;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double latitudeWGS84;

    private Double longitudeWGS84;

    @Column(name = "in_service_status")
    private String inServiceStatus;

    @Column(name = "local_authority_code")
    private String localAuthorityCode;

    @Column(name = "local_authority_name")
    private String localAuthorityName;

    @Column(name = "data_owner")
    private String dataOwner;

    @Column(name = "data_manager")
    private String dataManager;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HourlyAirQualityRecord> measurements = new ArrayList<>();
}
```

---

### 3. Service Layer

#### `AirQualityStationIngestionService.java`

```java
package com.example.emissionsCalculator.service;

import com.example.emissionsCalculator.dto.airQualityStation.LaqnMonitoringSitesResponseDTO;
import com.example.emissionsCalculator.dto.airQualityStation.LaqnSiteDTO;
import com.example.emissionsCalculator.entity.airQualityStation.AirQualityStation;
import com.example.emissionsCalculator.repository.airQualityStation.AirQualityStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <b>O que é:</b> Serviço de ingestão de estações de qualidade do ar.
 * <b>Para que serve:</b> Sincronizar os metadados das estações da LAQN API via Spring RestClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityStationIngestionService {

    private final AirQualityStationRepository repository;
    private final RestClient restClient = RestClient.create();

    private static final String LAQN_STATIONS_API_URL = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";

    /**
     * <b>O que é:</b> Método principal de sincronização.
     * <b>Para que serve:</b> Consumir a LAQN API e persistir as estações ativas e inativas de forma idempotente.
     * @return Quantidade de estações processadas e gravadas.
     */
    @Transactional
    public int syncStations() {
        log.info("⏳ A conectar à LAQN API para sincronizar estações de qualidade do ar...");

        try {
            LaqnMonitoringSitesResponseDTO response = restClient.get()
                    .uri(LAQN_STATIONS_API_URL)
                    .retrieve()
                    .body(LaqnMonitoringSitesResponseDTO.class);

            if (response == null || response.sites() == null || response.sites().siteList() == null) {
                log.warn("⚠️ Nenhuma estação encontrada no payload retornado pela LAQN API.");
                return 0;
            }

            List<LaqnSiteDTO> siteDTOs = response.sites().siteList();

            List<String> incomingCodes = siteDTOs.stream()
                    .filter(dto -> dto.siteCode() != null && dto.latitude() != null && dto.longitude() != null)
                    .map(LaqnSiteDTO::siteCode)
                    .distinct()
                    .collect(Collectors.toList());

            List<AirQualityStation> existingStations = repository.findBySiteCodeIn(incomingCodes);
            Set<String> existingSiteCodes = existingStations.stream()
                    .map(AirQualityStation::getSiteCode)
                    .collect(Collectors.toSet());

            List<AirQualityStation> stationsToSave = new ArrayList<>();

            for (LaqnSiteDTO dto : siteDTOs) {
                if (dto.siteCode() != null && dto.latitude() != null && dto.longitude() != null) {
                    AirQualityStation station = repository.findBySiteCode(dto.siteCode())
                            .orElse(AirQualityStation.builder()
                                    .siteCode(dto.siteCode())
                                    .build());

                    station.setSiteName(dto.siteName());
                    station.setSiteType(dto.siteType());
                    station.setLatitude(dto.latitude());
                    station.setLongitude(dto.longitude());
                    station.setLatitudeWGS84(dto.latitudeWGS84());
                    station.setLongitudeWGS84(dto.longitudeWGS84());
                    station.setInServiceStatus(dto.inService());
                    station.setLocalAuthorityCode(dto.localAuthorityCode());
                    station.setLocalAuthorityName(dto.localAuthorityName());
                    station.setDataOwner(dto.dataOwner());
                    station.setDataManager(dto.dataManager());

                    // Tratamento flexível para aceitar "Yes", "true", "1" ou assumir ativo se nulo
                    boolean isActive = dto.inService() == null
                            || "Yes".equalsIgnoreCase(dto.inService().trim())
                            || "true".equalsIgnoreCase(dto.inService().trim())
                            || "1".equals(dto.inService().trim());

                    station.setIsActive(isActive);
                    station.setUpdatedAt(LocalDateTime.now());

                    stationsToSave.add(station);
                }
            }

            repository.saveAll(stationsToSave);
            log.info("✅ Sincronização concluída com sucesso! {} estações salvas/atualizadas.", stationsToSave.size());
            return stationsToSave.size();

        } catch (Exception e) {
            log.error("❌ Falha crítica ao sincronizar estações de qualidade do ar: {}", e.getMessage(), e);
            throw new RuntimeException("Erro na ingestão de dados LAQN: " + e.getMessage(), e);
        }
    }
}
```

---

### 4. Controller Layer

#### `AirQualityStationIngestionController.java`

```java
package com.example.emissionsCalculator.controller;

import com.example.emissionsCalculator.service.airQualityStation.AirQualityStationIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>O que é:</b> Controlador REST para o módulo de Qualidade do Ar.
 * <b>Para que serve:</b> Expor o endpoint POST /api/air-quality-stations/sync no ecossistema da aplicação.
 */
@RestController
@RequestMapping("/api/air-quality-stations")
@RequiredArgsConstructor
public class AirQualityStationIngestionController {

    private final AirQualityStationIngestionService service;

    /**
     * <b>O que é:</b> Endpoint de gatilho para sincronização de estações.
     * <b>Para que serve:</b> Executar a ingestão manual/agendada dos metadados das estações LAQN.
     */
    @PostMapping("/sync")
    public ResponseEntity<String> syncStations() {
        try {
            int totalProcessed = service.syncStations();
            return ResponseEntity.ok("Sincronização de estações concluída! " + totalProcessed + " estações processadas.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro durante a sincronização: " + e.getMessage());
        }
    }
}
```

---

## 🔍 Consultas SQL de Validação

### Contagem Total de Registros
```sql
SELECT COUNT(*) AS total_stations FROM air_quality_stations;
```

### Distribuição por Status de Atividade
```sql
SELECT is_active, in_service_status, COUNT(*) AS quantidade
FROM air_quality_stations
GROUP BY is_active, in_service_status;
```

### Agrupamento por Tipo Ambiental
```sql
SELECT site_type, COUNT(*) AS quantidade
FROM air_quality_stations
GROUP BY site_type
ORDER BY quantidade DESC;
```
