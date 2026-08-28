package uk.org.spire.emissionsCalculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.org.spire.emissionsCalculator.dto.CurrentForecast;
import uk.org.spire.emissionsCalculator.dto.LondonAirQualityResponse;
import uk.org.spire.emissionsCalculator.integration.LondonAirQualityClient;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LondonDataController.class)
class LondonDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LondonAirQualityClient airQualityClient;

    @Test
    void shouldReturnLondonAirQualityData() throws Exception {
        // Dado: Preparamos um retorno falso (mock)
        CurrentForecast forecast = new CurrentForecast("Current", "Low", "Test Summary", "Low", "Low", "Low");
        LondonAirQualityResponse mockResponse = new LondonAirQualityResponse("hourly", "http://test", List.of(forecast));

        when(airQualityClient.fetchLondonAirQuality()).thenReturn(mockResponse);

        // Quando & Então: Simulamos o GET e validamos se o JSON sai perfeito
        mockMvc.perform(get("/api/v1/london/air-quality"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatePeriod").value("hourly"))
                .andExpect(jsonPath("$.currentForecast[0].forecastSummary").value("Test Summary"))
                .andExpect(jsonPath("$.currentForecast[0].pM25Band").value("Low"));
    }
}