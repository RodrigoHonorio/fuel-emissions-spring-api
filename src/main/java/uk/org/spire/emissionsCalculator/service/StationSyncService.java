package uk.org.spire.emissionsCalculator.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.org.spire.emissionsCalculator.dto.SiteDto;
import uk.org.spire.emissionsCalculator.dto.StationApiResponse;
import uk.org.spire.emissionsCalculator.model.SpatialNode;
import uk.org.spire.emissionsCalculator.repository.SpatialNodeRepository;

@Configuration
public class StationSyncService {

    @Bean
    CommandLineRunner fetchLondonStations(SpatialNodeRepository repository) {
        return args -> {
            String url = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";
            RestTemplate restTemplate = new RestTemplate();

            try {
                System.out.println(">>> Conectando à API de Londres para baixar todas as estações...");
                StationApiResponse response = restTemplate.getForObject(url, StationApiResponse.class);

                if (response != null && response.getSites() != null && response.getSites().getSiteList() != null) {
                    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
                    int count = 0;

                    for (SiteDto dto : response.getSites().getSiteList()) {
                        if (dto.getLatitude() != null && dto.getLongitude() != null) {
                            SpatialNode node = new SpatialNode(
                                    dto.getSiteName(),
                                    geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()))
                            );
                            repository.save(node);
                            count++;
                        }
                    }
                    System.out.println(">>> Sucesso! " + count + " estações reais de Londres foram importadas para o H2!");
                }
            } catch (Exception e) {
                System.err.println(">>> Erro ao sincronizar estações da API externa: " + e.getMessage());
            }
        };
    }
}