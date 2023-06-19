package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class ConnectionProviderRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 9092832923490064251L;

    public ConnectionProviderRuntimeException() {
    }

    public ConnectionProviderRuntimeException(String message) {
        super(message);
    }

    public ConnectionProviderRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionProviderRuntimeException(Throwable cause) {
        super(cause);
    }

    public ConnectionProviderRuntimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
