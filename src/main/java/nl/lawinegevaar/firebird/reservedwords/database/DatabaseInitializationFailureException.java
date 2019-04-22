package nl.lawinegevaar.firebird.reservedwords.database;

public class DatabaseInitializationFailureException extends RuntimeException {

    public DatabaseInitializationFailureException(String message) {
        super(message);
    }

    public DatabaseInitializationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
