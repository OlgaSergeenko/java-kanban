package exceptions;

public class TaskTimeValidationException extends RuntimeException {

    public TaskTimeValidationException(final String message) {
        super(message);
    }
}
