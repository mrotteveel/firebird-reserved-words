package nl.lawinegevaar.firebird.reservedwords;

public class KeywordProcessingException extends RuntimeException {

    public KeywordProcessingException(String message) {
        super(message);
    }

    public KeywordProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
