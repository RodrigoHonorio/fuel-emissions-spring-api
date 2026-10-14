package com.example.emissionsCalculator.dto.scientific;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> DTO para transferência dos dados da regra científica e metadados do artigo.
 */
public record ScientificRuleResponseDTO(
        Long id,
        String ruleId,
        String ruleName,
        String articleName,
        String pdfFileName,
        Boolean hasPdfAttached,
        String targetPollutant,
        String inputVariables,
        String mvelFormula,
        String unitOfMeasure,
        String description,
        LocalDateTime createdAt
) {}