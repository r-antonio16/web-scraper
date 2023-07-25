package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class LoadYamlTemplateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5477512216811751516L;


    public LoadYamlTemplateException(String message) {
        super(message);
    }

    public LoadYamlTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
