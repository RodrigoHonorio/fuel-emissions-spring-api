package com.example.emissionsCalculator.dto.boundary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnsBoroughFeatureDTO(
        OnsBoroughPropertiesDTO properties,
        Map<String, Object> geometry
) {}