package uk.org.spire.emissionsCalculator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.model.LondonBorough;
import uk.org.spire.emissionsCalculator.repository.LondonBoroughRepository;

import java.io.InputStream;

@Service
public class LondonBoroughLoaderService {

    private final LondonBoroughRepository boroughRepository;
    private final ObjectMapper objectMapper;

    public LondonBoroughLoaderService(LondonBoroughRepository boroughRepository, ObjectMapper objectMapper) {
        this.boroughRepository = boroughRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initBoroughs() {
        if (boroughRepository.count() > 0) {
            System.out.println("⚡ S.P.I.R.E.: Os bairros de Londres já estão carregados no PostGIS. Ignorando carga.");
            return;
        }

        try {
            // Coloque o ficheiro 'london-boroughs.geojson' dentro de src/main/resources
            ClassPathResource resource = new ClassPathResource("london-boroughs.geojson");
            if (!resource.exists()) {
                System.out.println("⚠️ Ficheiro 'london-boroughs.geojson' não encontrado em resources. Pulinando carga geográfica.");
                return;
            }

            InputStream inputStream = resource.getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode features = rootNode.get("features");

            if (features != null && features.isArray()) {
                GeoJsonReader reader = new GeoJsonReader();

                for (JsonNode feature : features) {
                    JsonNode properties = feature.get("properties");
                    JsonNode geomNode = feature.get("geometry");

                    if (properties != null && geomNode != null) {
                        String name = properties.has("name") ? properties.get("name").asText() : "Unknown";
                        String code = properties.has("code") ? properties.get("code").asText() : null;
                        String region = properties.has("region") ? properties.get("region").asText() : null;

                        // Converte o JSON da geometria para o formato JTS suportado pelo PostGIS/Hibernate
                        Geometry geometry = reader.read(geomNode.toString());

                        if (boroughRepository.findByName(name).isEmpty()) {
                            LondonBorough borough = new LondonBorough(name, code, region, geometry);
                            boroughRepository.save(borough);
                        }
                    }
                }
                System.out.println("✅ Bairros de Londres carregados com sucesso para o PostGIS!");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar o GeoJSON dos bairros de Londres: " + e.getMessage());
            e.printStackTrace();
        }
    }
}