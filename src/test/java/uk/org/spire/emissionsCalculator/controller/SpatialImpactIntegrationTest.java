package uk.org.spire.emissionsCalculator.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SpatialImpactIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve calcular com sucesso o impacto de emissão e retornar a estação mais próxima")
    void shouldCalculateEmissionImpactSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/emissions/impact")
                        .param("fuelType", "Petrol")
                        .param("volume", "2000")
                        .param("latitude", "51.5074")
                        .param("longitude", "-0.1278")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmissionsKg").exists())
                .andExpect(jsonPath("$.closestStationName").exists())
                .andExpect(jsonPath("$.distanceKm").exists());
    }

    @Test
    @DisplayName("Deve retornar os nós espaciais para o heatmap")
    void shouldReturnSpatialNodesForHeatmap() throws Exception {
        mockMvc.perform(get("/api/v1/spatial/nodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}