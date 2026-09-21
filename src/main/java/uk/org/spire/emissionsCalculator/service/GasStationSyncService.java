package uk.org.spire.emissionsCalculator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.org.spire.emissionsCalculator.model.GasStation;
import uk.org.spire.emissionsCalculator.repository.GasStationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GasStationSyncService {

    private final GasStationRepository gasStationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> OVERPASS_MIRRORS = List.of(
            "https://overpass.kumi.systems/api/interpreter",
            "https://overpass-api.de/api/interpreter",
            "https://overpass.private.coffee/api/interpreter"
    );

    public GasStationSyncService(GasStationRepository gasStationRepository) {
        this.gasStationRepository = gasStationRepository;
    }

    // Executa ao iniciar o sistema se a tabela estiver vazia
    @EventListener(ApplicationReadyEvent.class)
    public void initSyncIfEmpty() {
        if (gasStationRepository.count() == 0) {
            System.out.println("📦 Banco de dados sem postos. A iniciar carga inicial do OpenStreetMap...");
            syncGasStations();
        } else {
            System.out.println("✅ Banco de dados já contém " + gasStationRepository.count() + " postos cadastrados.");
        }
    }

    // Executa automaticamente todos os domingos às 03:00 da manhã
    @Scheduled(cron = "0 0 3 * * SUN")
    public void syncGasStations() {
        System.out.println("🔄 Sincronizando postos de combustível com o OpenStreetMap...");
        String query = "[out:json][timeout:60];node[\"amenity\"=\"fuel\"](51.28,-0.51,51.69,0.33);out body;";

        String jsonResponse = fetchFromOverpassWithFallback(query);

        if (jsonResponse == null) {
            System.err.println("❌ Falha ao obter dados de todos os servidores do Overpass.");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode elements = root.path("elements");

            int savedCount = 0;
            for (JsonNode node : elements) {
                Long osmId = node.path("id").asLong();
                Double lat = node.path("lat").asDouble();
                Double lon = node.path("lon").asDouble();

                JsonNode tags = node.path("tags");
                String name = tags.has("name") ? tags.path("name").asText() :
                        (tags.has("brand") ? tags.path("brand").asText() : "Posto de Combustível");
                String operator = tags.has("operator") ? tags.path("operator").asText() :
                        (tags.has("brand") ? tags.path("brand").asText() : "Independente");

                int pumps = extractOrEstimatePumps(tags, operator);

                Optional<GasStation> existing = gasStationRepository.findByOsmId(osmId);
                GasStation station = existing.orElseGet(() -> new GasStation(osmId, name, operator, lat, lon));

                station.setName(name);
                station.setOperator(operator);
                station.setLatitude(lat);
                station.setLongitude(lon);
                station.setNumberOfPumps(pumps);

                gasStationRepository.save(station);
                savedCount++;
            }

            System.out.println("✅ Sincronização concluída com sucesso! Total de postos na BD: " + savedCount);

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar o JSON do OSM: " + e.getMessage());
        }
    }

    private String fetchFromOverpassWithFallback(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        for (String mirrorUrl : OVERPASS_MIRRORS) {
            try {
                HttpEntity<String> request = new HttpEntity<>("data=" + query, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(mirrorUrl, request, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (Exception e) {
                System.err.println("⚠️ Mirror " + mirrorUrl + " indisponível. A tentar o próximo...");
            }
        }
        return null;
    }

    private int extractOrEstimatePumps(JsonNode tags, String operator) {
        if (tags.has("capacity:pumps")) {
            return tags.path("capacity:pumps").asInt(6);
        } else if (tags.has("pumps")) {
            return tags.path("pumps").asInt(6);
        }

        String opUpper = operator.toUpperCase();
        if (opUpper.contains("TESCO") || opUpper.contains("ASDA") || opUpper.contains("SAINSBURY") || opUpper.contains("MORRISONS")) {
            return 12;
        } else if (opUpper.contains("BP") || opUpper.contains("SHELL") || opUpper.contains("ESSO")) {
            return 8;
        } else if (opUpper.contains("TEXACO") || opUpper.contains("JET")) {
            return 6;
        } else {
            return 4;
        }
    }
}