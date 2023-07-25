package pt.zerodseis.services.webscraper.exceptions;

import java.io.Serial;
import java.util.List;

public class YamlTemplateValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5784833000935853478L;

    private final String templatePath;
    private final List<String> errors;

    public YamlTemplateValidationException(String templatePath, List<String> errors) {
        super();
        this.templatePath = templatePath;
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return String.format("Template %s is not valid. Errors: %s", templatePath, errors);
    }
}
