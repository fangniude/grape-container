package org.grape;

/**
 * Plugin runtime exception
 *
 * @author lewis
 */
public final class GrapeException extends RuntimeException {
    private static final long serialVersionUID = -3001339513837419069L;

    public GrapeException() {
        super();
    }

    public GrapeException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrapeException(String message) {
        super(message);
    }

    public GrapeException(Throwable cause) {
        super(cause);
    }

}
