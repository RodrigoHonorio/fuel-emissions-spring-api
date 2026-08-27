package uk.org.spire.emissionsCalculator.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for standardising error responses sent to the client.
 *
 * @param timestamp The exact time when the error occurred.
 * @param status    The HTTP status code.
 * @param error     The error type or reason.
 * @param message   The descriptive error message.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}