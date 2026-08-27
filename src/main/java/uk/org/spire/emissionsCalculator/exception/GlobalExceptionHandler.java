package uk.org.spire.emissionsCalculator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.org.spire.emissionsCalculator.dto.ErrorResponse;

import java.time.LocalDateTime;

/**
 * Global exception handler for managing application-wide error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles custom validation exceptions for emission data.
   *
   * @param ex The thrown {@link InvalidEmissionDataException}.
   * @return A {@link ResponseEntity} containing a structured {@link ErrorResponse} with HTTP 400 Bad Request.
   */
  @ExceptionHandler(InvalidEmissionDataException.class)
  public ResponseEntity<ErrorResponse> handleInvalidEmissionData(InvalidEmissionDataException ex) {
    ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage()
    );
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles any unexpected general exceptions across the system.
   *
   * @param ex The thrown {@link Exception}.
   * @return A {@link ResponseEntity} containing a structured {@link ErrorResponse} with HTTP 500 Internal Server Error.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred: " + ex.getMessage()
    );
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
