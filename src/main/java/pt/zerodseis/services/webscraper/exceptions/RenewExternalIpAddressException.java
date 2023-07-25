package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class RenewExternalIpAddressException extends ConnectionProviderRuntimeException {

    @Serial
    private static final long serialVersionUID = -1126994264151491897L;

    public RenewExternalIpAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}
