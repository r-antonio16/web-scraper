package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class SiteConnectionException extends ConnectionProviderRuntimeException {

    @Serial
    private static final long serialVersionUID = 1290720024840017694L;

    public SiteConnectionException(String message) {
        super(message);
    }

    public SiteConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
