package uk.org.spire.emissionsCalculator.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Spatial Node REST controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpatialNodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should successfully retrieve all spatial nodes synchronised from the API")
    void shouldReturnSpatialNodesList() throws Exception {
        mockMvc.perform(get("/api/v1/spatial/nodes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}