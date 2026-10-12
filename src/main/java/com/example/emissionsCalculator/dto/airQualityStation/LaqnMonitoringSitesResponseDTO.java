package com.example.emissionsCalculator.dto.airQualityStation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <b>O que é:</b> DTO de topo para a resposta completa da LAQN API.
 * <b>Para que serve:</b> Ponto de entrada do payload JSON para sincronização de estações.
 * <b>De onde veio:</b> Resposta raiz do endpoint https://api.erg.ic.ac.uk/...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LaqnMonitoringSitesResponseDTO(
        @JsonProperty("Sites")
        LaqnSitesWrapperDTO sites
) {}