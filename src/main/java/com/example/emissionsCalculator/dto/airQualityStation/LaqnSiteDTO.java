package com.example.emissionsCalculator.dto.airQualityStation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <b>O que é:</b> DTO para desserialização dos metadados de uma estação da London Air Quality Network (LAQN).
 * <b>Para que serve:</b> Mapear 100% dos atributos do nó 'Site' retornado pelo Imperial College London (Data First).
 * <b>De onde veio:</b> Elemento 'Site' dentro da resposta JSON da LAQN API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LaqnSiteDTO(
        @JsonProperty("@SiteCode")
        String siteCode,

        @JsonProperty("@SiteName")
        String siteName,

        @JsonProperty("@SiteType")
        String siteType,

        @JsonProperty("@Latitude")
        Double latitude,

        @JsonProperty("@Longitude")
        Double longitude,

        @JsonProperty("@LatitudeWGS84")
        Double latitudeWGS84,

        @JsonProperty("@LongitudeWGS84")
        Double longitudeWGS84,

        @JsonProperty("@InService")
        String inService,

        @JsonProperty("@LocalAuthorityCode")
        String localAuthorityCode,

        @JsonProperty("@LocalAuthorityName")
        String localAuthorityName,

        @JsonProperty("@DataOwner")
        String dataOwner,

        @JsonProperty("@DataManager")
        String dataManager
) {}