package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class GetExternalIpAddressException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1290720024840017694L;

    public GetExternalIpAddressException() {
    }

    public GetExternalIpAddressException(String message) {
        super(message);
    }

    public GetExternalIpAddressException(String message, Throwable cause) {
        super(message, cause);
    }

    public GetExternalIpAddressException(Throwable cause) {
        super(cause);
    }

    public GetExternalIpAddressException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
