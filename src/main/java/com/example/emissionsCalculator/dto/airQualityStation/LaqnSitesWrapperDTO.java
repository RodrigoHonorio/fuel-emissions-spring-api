package com.example.emissionsCalculator.dto.airQualityStation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * <b>O que é:</b> DTO invólucro para a coleção de estações da LAQN.
 * <b>Para que serve:</b> Descompactar o nó 'Sites' do JSON, garantindo desserialização segura de listas.
 * <b>De onde veio:</b> Objeto 'Sites' na resposta da LAQN API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LaqnSitesWrapperDTO(
        @JsonProperty("@GroupName")
        String groupName,

        @JsonProperty("Site")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<LaqnSiteDTO> siteList
) {}