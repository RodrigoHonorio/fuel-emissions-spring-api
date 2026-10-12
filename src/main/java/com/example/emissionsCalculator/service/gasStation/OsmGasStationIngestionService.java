package com.example.emissionsCalculator.service.gasStation;

import com.example.emissionsCalculator.dto.gasStation.OverpassResponseDTO;
import com.example.emissionsCalculator.entity.gasStation.GasStation;
import com.example.emissionsCalculator.repository.gasStation.GasStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>O que é:</b> Serviço de ingestão de dados geográficos do OpenStreetMap (OSM).
 * <b>Para que serve:</b> Consultar a API pública Overpass por Bounding Box, desserializar o JSON no DTO e salvar os postos da Grande Londres na tabela 'gas_stations'[cite: 17, 19, 24].
 * <b>De onde veio:</b> Servidores do OpenStreetMap Overpass API[cite: 24].
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OsmGasStationIngestionService {

    /** Repositório de persistência JPA[cite: 21, 24]. */
    private final GasStationRepository repository;

    /** Cliente HTTP moderno do Spring Boot 3[cite: 24]. */
    private final RestClient restClient = RestClient.create();

    /** Bounding Box cobrindo toda a Grande Londres (South, West, North, East)[cite: 24]. */
    private static final String LONDON_BBOX = "51.28676,-0.510375,51.691874,0.334015";

    /** Endpoint público da API Overpass[cite: 24]. */
    private static final String OVERPASS_ENDPOINT = "https://overpass-api.de/api/interpreter";

    /**
     * <b>O que é:</b> Método principal de sincronização de Londres[cite: 24].
     * <b>Para que serve:</b> Executar a consulta Bounding Box e persistir os postos no banco[cite: 24].
     * <b>De onde veio:</b> Chamado pelo controlador 'OsmGasStationIngestionController'.
     *
     * @return Quantidade de postos salvos na base de dados.
     */
    public int syncOsmGasStationsForLondon() {
        String query = String.format("""
            [out:json][timeout:30];
            (
              node["amenity"="fuel"](%s);
              way["amenity"="fuel"](%s);
            );
            out center;
            """, LONDON_BBOX, LONDON_BBOX);

        try {
            log.info("⏳ A obter postos de combustível de Londres do Overpass...");

            OverpassResponseDTO response = restClient.get()
                    .uri(OVERPASS_ENDPOINT + "?data={query}", query)
                    .retrieve()
                    .body(OverpassResponseDTO.class);

            if (response != null && response.elements() != null) {
                List<GasStation> stations = parseStations(response.elements());
                repository.saveAll(stations);
                log.info("✅ Sincronização de Londres concluída! {} postos salvos.", stations.size());
                return stations.size();
            }
        } catch (Exception e) {
            log.error("❌ Falha ao sincronizar com Overpass: {}", e.getMessage());
            throw new RuntimeException("Erro ao conectar ao Overpass: " + e.getMessage());
        }

        return 0;
    }

    /**
     * <b>O que é:</b> Método interno de conversão de DTO para Entidade JPA[cite: 24].
     * <b>Para que serve:</b> Extrair lat/lon, tags e aplicar fallback 'London' para cidades em branco[cite: 24].
     * <b>De onde veio:</b> Lógica interna do serviço[cite: 24].
     */
    private List<GasStation> parseStations(List<OverpassResponseDTO.Element> elements) {
        List<GasStation> list = new ArrayList<>();

        for (var element : elements) {
            Double lat = element.lat() != null ? element.lat() : (element.center() != null ? element.center().lat() : null);
            Double lon = element.lon() != null ? element.lon() : (element.center() != null ? element.center().lon() : null);

            if (lat == null || lon == null) continue;

            Map<String, String> tags = element.tags() != null ? element.tags() : Map.of();

            // Garantia do requisito: se o campo addr:city vier em branco ou nulo, atribui "London"
            String city = tags.getOrDefault("addr:city", "").isBlank() ? "London" : tags.get("addr:city");

            GasStation station = GasStation.builder()
                    .id(element.id())
                    .latitude(lat)
                    .longitude(lon)
                    .amenity("fuel")
                    .name(tags.getOrDefault("name", "Desconhecido"))
                    .brand(tags.getOrDefault("brand", "N/A"))
                    .operator(tags.get("operator"))
                    .street(tags.get("addr:street"))
                    .postcode(tags.get("addr:postcode"))
                    .city(city)
                    .openingHours(tags.get("opening_hours"))
                    .phone(tags.get("phone"))
                    .website(tags.get("website"))
                    .hasDiesel("yes".equalsIgnoreCase(tags.get("fuel:diesel")))
                    .hasGasoline95("yes".equalsIgnoreCase(tags.get("fuel:octane_95")))
                    .hasGasoline98("yes".equalsIgnoreCase(tags.get("fuel:octane_98")))
                    .hasLpg("yes".equalsIgnoreCase(tags.get("fuel:lpg")))
                    .hasElectric("yes".equalsIgnoreCase(tags.get("fuel:electricity")))
                    .hasCarWash("yes".equalsIgnoreCase(tags.get("car_wash")))
                    .hasCompressedAir("yes".equalsIgnoreCase(tags.get("compressed_air")))
                    .hasShop(tags.containsKey("shop") && !"no".equalsIgnoreCase(tags.get("shop")))
                    .build();

            list.add(station);
        }
        return list;
    }
}