package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class InvalidURLException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8972670484880434729L;

    public InvalidURLException() {
    }

    public InvalidURLException(String message) {
        super(message);
    }

    public InvalidURLException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidURLException(Throwable cause) {
        super(cause);
    }

    public InvalidURLException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
