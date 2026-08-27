package uk.org.spire.emissionsCalculator.exception;

/**
 * Custom exception thrown when invalid emission calculation parameters are provided.
 */
public class InvalidEmissionDataException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public InvalidEmissionDataException(String message) {
        super(message);
    }
}
