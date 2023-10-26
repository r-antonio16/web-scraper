package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class SiteConnectionInitException extends ConnectionProviderRuntimeException {

    @Serial
    private static final long serialVersionUID = 2755608985904294234L;

    public SiteConnectionInitException(String message) {
        super(message);
    }

    public SiteConnectionInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
