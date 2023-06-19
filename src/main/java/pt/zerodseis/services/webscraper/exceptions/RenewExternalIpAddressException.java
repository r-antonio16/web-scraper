package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class RenewExternalIpAddressException extends ConnectionProviderRuntimeException {

    @Serial
    private static final long serialVersionUID = -1126994264151491897L;

    public RenewExternalIpAddressException() {
    }

    public RenewExternalIpAddressException(String message) {
        super(message);
    }

    public RenewExternalIpAddressException(String message, Throwable cause) {
        super(message, cause);
    }

    public RenewExternalIpAddressException(Throwable cause) {
        super(cause);
    }

    public RenewExternalIpAddressException(String message, Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
