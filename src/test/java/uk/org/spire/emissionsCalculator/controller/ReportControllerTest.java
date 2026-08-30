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

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve gerar e baixar o relatório oficial em PDF com sucesso")
    void shouldGeneratePdfReportSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/reports/pdf")
                        .param("fuelType", "Petrol")
                        .param("volume", "2000")
                        .param("totalEmissionsKg", "540.5")
                        .param("closestStationName", "Brent - Ikea")
                        .param("distanceKm", "2.4")
                        .param("latitude", "51.5074")
                        .param("longitude", "-0.1278"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"SPIRE_Impact_Report.pdf\""));
    }
}