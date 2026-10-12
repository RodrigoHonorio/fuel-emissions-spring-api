package com.example.emissionsCalculator.dto.boundary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnsBoroughResponseDTO(
        List<OnsBoroughFeatureDTO> features
) {}