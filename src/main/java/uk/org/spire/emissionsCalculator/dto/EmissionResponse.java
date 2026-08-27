package uk.org.spire.emissionsCalculator.dto;

/**
 * Data Transfer Object (DTO) for sending the calculated emission results back to the client.
 *
 * @param estimatedVocEmissionsKg The calculated VOC emissions in kilograms.
 */
public record EmissionResponse(
        double estimatedVocEmissionsKg
) {
}