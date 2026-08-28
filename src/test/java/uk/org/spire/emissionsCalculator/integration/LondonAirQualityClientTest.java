package uk.org.spire.emissionsCalculator.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import uk.org.spire.emissionsCalculator.dto.LondonAirQualityResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(LondonAirQualityClient.class)
class LondonAirQualityClientTest {

    @Autowired
    private LondonAirQualityClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldFetchAndMapAirQualityData() {
        // Dado: O JSON sujo que o governo de Londres nos enviaria
        String mockJson = """
                {
                    "updatePeriod": "hourly",
                    "forecastURL": "http://londonair.org.uk",
                    "currentForecast": [
                        {
                            "forecastType": "Current",
                            "forecastBand": "Low",
                            "forecastSummary": "Low air pollution test",
                            "nO2Band": "Low",
                            "pM10Band": "Low",
                            "pM25Band": "High"
                        }
                    ]
                }
                """;

        // Intercepta a requisição e devolve o JSON falso
        server.expect(requestTo("https://api.tfl.gov.uk/AirQuality"))
                .andRespond(withSuccess(mockJson, MediaType.APPLICATION_JSON));

        // Quando: Acionamos o cliente
        LondonAirQualityResponse response = client.fetchLondonAirQuality();

        // Então: Validamos se os Records filtraram e mapearam tudo corretamente
        assertNotNull(response);
        assertEquals("hourly", response.updatePeriod());
        assertEquals("Low air pollution test", response.currentForecast().get(0).forecastSummary());
        assertEquals("High", response.currentForecast().get(0).pM25Band()); // Simulando alerta de PM2.5
    }
}
