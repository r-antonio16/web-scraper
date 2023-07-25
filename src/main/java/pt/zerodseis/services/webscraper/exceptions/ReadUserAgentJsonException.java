package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class ReadUserAgentJsonException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6640689715023350755L;

    public ReadUserAgentJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
