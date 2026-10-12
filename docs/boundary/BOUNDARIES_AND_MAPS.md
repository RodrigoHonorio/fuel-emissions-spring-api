# Documentação Técnica e Científica: Ingestão de Limites Territoriais e Cartografia Urbana (London Administrative Boundaries & Maps)

---

## 1. Visão Geral para Pesquisadores e Analistas Geográficos (Sem Necessidade de Programação)

### 1.1. Contexto e Origem dos Dados
A análise espacial integrada de postos de combustível, medições de qualidade do ar e variáveis meteorológicas exige uma **estrutura cartográfica de referência**. Este módulo realiza a ingestão e estruturação dos limites administrativos oficiais da Região Metropolitana de Londres (*Greater London Boroughs*), obtidos a partir das bases abertas do **ONS (Office for National Statistics - Open Geography Portal)** e do **Ordnance Survey (OS Boundary-Line)**.

A delimitação precisa dos distritos (*boroughs*) e zonas censitárias permite agrupar dados pontuais (como coordenadas de postos ou estações) em polígonos territoriais oficiais para análises socioeconômicas e de impacto ambiental.

### 1.2. Definição do Escopo Espacial e Sistemas de Projeção
* **Sistema de Referência Espacial Nativo do UK:** British National Grid (EPSG:27700 / OSGB36), utilizado para projeções planares precisas em metros no Reino Unido.
* **Sistema de Projeção Global Armazenado:** WGS 84 (EPSG:4326), padrão internacional em graus decimais para interoperabilidade com o ecossistema GPS, OpenStreetMap e APIs web.
* **Resolução Cartográfica:** Limites poligonais simplificados para alta performance vetorial sem perda de precisão topológica regional.

### 1.3. Dicionário de Dados e Atributos Territoriais
| Atributo / Campo | Definição Geográfica / Regulatória | Aplicação em Pesquisa |
| :--- | :--- | :--- |
| **GSS Code (`gss_code`)** | Código único padronizado pelo governo britânico (ex: `E09000001` para City of London). | Agrupamento estatístico (*Join*) com dados censitários e demográficos do ONS. |
| **Borough Name (`name`)** | Nome oficial do distrito administrativo (ex: Westminster, Camden, Greenwich). | Categorização territorial e geração de relatórios por autoridade local. |
| **Area ($km^2$) (`area_sqkm`)** | Área da superfície do polígono calculada em quilômetros quadrados. | Cálculo de densidade de postos por $km^2$ e vazão de emissões por área. |
| **Geometry (`geometry`)** | Objeto geométrico vetorial do tipo `MultiPolygon` armazenado em formato geodésico. | Execução de consultas espaciais (*Point-in-Polygon*, *Buffer*, *Intersection*). |

---

## 2. Visão Técnica para Engenheiros de Software e Desenvolvedores

### 2.1. Arquitetura da Solução e Fluxo de Dados Espaciais
A ingestão realiza o consumo de dados cartográficos via **GeoJSON REST Service**, convertendo feições geográficas em entidades JPA espaciais persistidas via **PostGIS** (extensão espacial do PostgreSQL).

```
[ ONS / Ordnance Survey GeoJSON API ]
         │ (HTTP GET - GeoJSON FeatureCollection)
         ▼
[ GeoJsonFeatureCollectionDto ] ─── (Parsing Jackson / GeoJSON)
         │
         ▼
[ BoundaryIngestionService ] ────── (Transformação Topológica & JTS Geometry)
         │
         ▼
[ LondonBorough (JPA + PostGIS) ] ── (Persistência da Geometria MultiPolygon EPSG:4326)
         │
         ▼
[ PostgreSQL + PostGIS (Tabela: london_boroughs) ]
```

### 2.2. Suporte a Geometria Espacial no Spring Boot
O módulo utiliza a biblioteca **JTS (Java Topology Suite)** integrada com **Hibernate Spatial**:
* **Tipo Geométrico:** `org.locationtech.jts.geom.MultiPolygon`.
* **Mapeamento SQL:** Tipo nativo `geometry(MultiPolygon, 4326)` no PostgreSQL.
* **Indexação Espacial:** Criação automática de índices **GIST** (*Generalized Search Tree*) na coluna `geometry` para acelerar operações topológicas `$O(\log N)$`.

---

## 3. Estrutura de Código e Componentes do Módulo

### 3.1. DTOs de Ingestão Cartográfica

#### `GeoJsonFeatureDto.java`

```java
package com.example.emissionsCalculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO para mapeamento de uma Feature GeoJSON individual contendo propriedades e geometria.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoJsonFeatureDto(
        @JsonProperty("type") String type,
        @JsonProperty("properties") Map<String, Object> properties,
        @JsonProperty("geometry") Map<String, Object> geometry
) {
}
```

#### `GeoJsonFeatureCollectionDto.java`

```java
package com.example.emissionsCalculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO raiz para a resposta da API de GeoJSON (Collection de limites territoriais).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoJsonFeatureCollectionDto(
        @JsonProperty("type") String type,
        @JsonProperty("features") List<GeoJsonFeatureDto> features
) {
}
```

---

### 3.2. Entidade JPA Espacial

#### `LondonBorough.java`

```java
package com.example.emissionsCalculator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.MultiPolygon;

import java.time.LocalDateTime;

/**
 * Entidade JPA para mapeamento dos limites territoriais dos Boroughs de Londres no PostGIS.
 */
@Entity
@Table(name = "london_boroughs", indexes = {
        @Index(name = "idx_borough_gss_code", columnList = "gss_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LondonBorough {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gss_code", nullable = false, unique = true, length = 20)
    private String gssCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "area_sqkm")
    private Double areaSqKm;

    @Column(name = "geometry", columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geometry;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

---

### 3.3. camada de Repositório Espacial

#### `LondonBoroughRepository.java`

```java
package com.example.emissionsCalculator.repository;

import com.example.emissionsCalculator.entity.LondonBorough;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LondonBoroughRepository extends JpaRepository<LondonBorough, Long> {

    Optional<LondonBorough> findByGssCode(String gssCode);

    /**
     * Consulta PostGIS: Identifica em qual Borough um ponto (Longitude/Latitude) está contido.
     */
    @Query(value = """
            SELECT * FROM london_boroughs b
            WHERE ST_Contains(b.geometry, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))
            LIMIT 1
            """, nativeQuery = true)
    Optional<LondonBorough> findBoroughContainingPoint(@Param("latitude") Double latitude,
                                                       @Param("longitude") Double longitude);
}
```

---

### 3.4. Camada de Serviço

#### `BoundaryIngestionService.java`

```java
package com.example.emissionsCalculator.service;

import com.example.emissionsCalculator.dto.GeoJsonFeatureCollectionDto;
import com.example.emissionsCalculator.dto.GeoJsonFeatureDto;
import com.example.emissionsCalculator.entity.LondonBorough;
import com.example.emissionsCalculator.repository.LondonBoroughRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoundaryIngestionService {

    private final LondonBoroughRepository repository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private static final String BOUNDARIES_API_URL =
            "https://services1.arcgis.com/ESMARspOiHYMwA3A/arcgis/rest/services/Local_Authority_Districts_December_2021_GB_BFC/FeatureServer/0/query?where=LAD21NM%20LIKE%20'%25'&outFields=LAD21CD,LAD21NM&outSR=4326&f=geojson";

    @Transactional
    public int syncLondonBoroughs() {
        log.info("⏳ A conectar à API GeoJSON para sincronizar limites dos Boroughs de Londres...");

        try {
            GeoJsonFeatureCollectionDto response = restClient.get()
                    .uri(BOUNDARIES_API_URL)
                    .retrieve()
                    .body(GeoJsonFeatureCollectionDto.class);

            if (response == null || response.features() == null) {
                log.warn("⚠️ Nenhuma feição geográfica retornada pela API de Limites.");
                return 0;
            }

            GeoJsonReader reader = new GeoJsonReader(geometryFactory);
            List<LondonBorough> boroughsToSave = new ArrayList<>();

            for (GeoJsonFeatureDto feature : response.features()) {
                String gssCode = (String) feature.properties().get("LAD21CD");
                String name = (String) feature.properties().get("LAD21NM");

                if (gssCode != null && name != null) {
                    String jsonGeometry = objectMapper.writeValueAsString(feature.geometry());
                    Geometry parsedGeometry = reader.read(jsonGeometry);
                    parsedGeometry.setSRID(4326);

                    MultiPolygon multiPolygon = convertToMultiPolygon(parsedGeometry);

                    LondonBorough borough = repository.findByGssCode(gssCode)
                            .orElse(LondonBorough.builder().gssCode(gssCode).build());

                    borough.setName(name);
                    borough.setGeometry(multiPolygon);
                    borough.setAreaSqKm(parsedGeometry.getArea() * 111.0 * 111.0); // Aproximação de área em km²
                    borough.setUpdatedAt(LocalDateTime.now());

                    boroughsToSave.add(borough);
                }
            }

            repository.saveAll(boroughsToSave);
            log.info("✅ Limites territoriais sincronizados com sucesso! {} distritos salvos.", boroughsToSave.size());
            return boroughsToSave.size();

        } catch (Exception e) {
            log.error("❌ Erro ao sincronizar limites cartográficos: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na ingestão de limites cartográficos: " + e.getMessage(), e);
        }
    }

    private MultiPolygon convertToMultiPolygon(Geometry geom) {
        if (geom instanceof MultiPolygon mp) {
            return mp;
        } else if (geom instanceof org.locationtech.jts.geom.Polygon p) {
            return geometryFactory.createMultiPolygon(new org.locationtech.jts.geom.Polygon[]{p});
        }
        throw new IllegalArgumentException("Tipo geométrico não suportado: " + geom.getGeometryType());
    }
}
```

---

### 3.5. Camada de Controle REST

#### `BoundaryIngestionController.java`

```java
package com.example.emissionsCalculator.controller;

import com.example.emissionsCalculator.service.BoundaryIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boundaries/london-boroughs")
@RequiredArgsConstructor
public class BoundaryIngestionController {

    private final BoundaryIngestionService service;

    @PostMapping("/sync")
    public ResponseEntity<String> syncBoroughs() {
        try {
            int total = service.syncLondonBoroughs();
            return ResponseEntity.ok("Sincronização de limites cartográficos concluída! " + total + " distritos processados.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro na sincronização cartográfica: " + e.getMessage());
        }
    }
}
```

---

## 4. Instruções de Execução, Validação e Consultas Espaciais PostGIS

### 4.1. Como Executar a Ingestão
* **Via Swagger UI:** Acesse `http://localhost:8081/swagger-ui/index.html` $\rightarrow$ `boundary-ingestion-controller` $\rightarrow$ `POST /api/boundaries/london-boroughs/sync` $\rightarrow$ **Execute**.
* **Via cURL:**
  ```bash
  curl -X POST "http://localhost:8081/api/boundaries/london-boroughs/sync"
  ```

### 4.2. Consultas PostGIS Avançadas de Validação Inter-Módulos

#### 1. Associar Postos de Combustível aos seus Respetivos Boroughs (*Point-in-Polygon Join*)
```sql
SELECT 
    b.name AS borough_name,
    COUNT(g.id) AS total_gas_stations
FROM london_boroughs b
LEFT JOIN gas_stations g 
    ON ST_Contains(b.geometry, ST_SetSRID(ST_MakePoint(g.longitude, g.latitude), 4326))
GROUP BY b.name
ORDER BY total_gas_stations DESC;
```

#### 2. Associar Estações de Qualidade do Ar (LAQN) aos Distritos
```sql
SELECT 
    b.name AS borough_name,
    a.site_code,
    a.site_name,
    a.site_type
FROM air_quality_stations a
JOIN london_boroughs b 
    ON ST_Contains(b.geometry, ST_SetSRID(ST_MakePoint(a.longitude, a.latitude), 4326))
ORDER BY b.name;
```

#### 3. Exportar Limite Territorial em Formato GeoJSON para Visualização no QGIS / Leaflet
```sql
SELECT 
    name, 
    gss_code, 
    ST_AsGeoJSON(geometry) AS geojson_geometry
FROM london_boroughs;
```
