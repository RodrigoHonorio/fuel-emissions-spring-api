package uk.org.spire.emissionsCalculator.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.org.spire.emissionsCalculator.dto.LondonAirQualityResponse;

@Service
public class LondonAirQualityClient {

    private final RestClient restClient;
    private final String apiUrl;

    // O Spring injeta o valor do application.properties automaticamente aqui
    public LondonAirQualityClient(
            RestClient.Builder builder,
            @Value("${tfl.api.url}") String apiUrl) {
        this.restClient = builder.build();
        this.apiUrl = apiUrl;
    }

    public LondonAirQualityResponse fetchLondonAirQuality() {
        return restClient.get()
                .uri(apiUrl) // Usando a variável em vez da string fixa
                .retrieve()
                .body(LondonAirQualityResponse.class);
    }
}