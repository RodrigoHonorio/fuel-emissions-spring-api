package uk.org.spire.emissionsCalculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.org.spire.emissionsCalculator.constant.FuelType;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;
import uk.org.spire.emissionsCalculator.service.EmissionCalculationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test class for {@link EmissionsController}.
 * <p>
 * Tests the REST endpoint behavior and HTTP status codes.
 * </p>
 */
@WebMvcTest(EmissionsController.class)
class EmissionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmissionCalculationService emissionCalculationService;

    @Test
    void shouldCalculateEmissionsSuccessfully() throws Exception {
        PetrolStationEmission mockEmission = new PetrolStationEmission(500.0, 25.0, 1.05);
        mockEmission.setId(1L);

        // Atualizado para 7 parâmetros: FuelType, volume, temperatura, windSpeed, windDirection, latitude, longitude
        when(emissionCalculationService.calculateAndSaveEmissions(
                any(FuelType.class),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble()
        )).thenReturn(mockEmission);

        String jsonRequest = """
                {
                    "fuelType": "PETROL",
                    "fuelVolumeLitres": 500.0,
                    "ambientTemperatureCelsius": 25.0,
                    "windSpeed": 5.0,
                    "windDirection": 180.0,
                    "latitude": 51.5074,
                    "longitude": -0.1278
                }
                """;

        mockMvc.perform(post("/api/emissions/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estimatedVocEmissionsKg").value(1.05));
    }
}