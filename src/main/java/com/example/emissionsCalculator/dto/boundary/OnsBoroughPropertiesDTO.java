package com.example.emissionsCalculator.dto.boundary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnsBoroughPropertiesDTO(
        @JsonProperty("LAD22CD") String boroughCode,
        @JsonProperty("LAD22NM") String boroughName
) {}