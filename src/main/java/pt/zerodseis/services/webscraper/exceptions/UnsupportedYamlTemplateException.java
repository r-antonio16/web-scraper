package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;

public class UnsupportedYamlTemplateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4321538268944834813L;

    public UnsupportedYamlTemplateException(String message) {
        super(message);
    }
}
