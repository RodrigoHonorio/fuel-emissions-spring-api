package uk.org.spire.emissionsCalculator.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles custom validation exceptions for emission data.
   */
  @ExceptionHandler(InvalidEmissionDataException.class)
  public ResponseEntity<ErrorResponse> handleInvalidEmissionData(InvalidEmissionDataException ex) {
    log.error(">>> ERRO DE VALIDAÇÃO CAPTURADO: {}", ex.getMessage(), ex);
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
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    log.error(">>> ERRO INTERNO NÃO TRATADO CAPTURADO: {}", ex.getMessage(), ex);
    ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred: " + ex.getMessage()
    );
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}