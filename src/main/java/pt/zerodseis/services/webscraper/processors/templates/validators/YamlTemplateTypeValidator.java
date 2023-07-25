package pt.zerodseis.services.webscraper.processors.templates.validators;

import java.util.Map;
import pt.zerodseis.services.webscraper.processors.templates.TemplateType;

public interface YamlTemplateTypeValidator {

    TemplateType getType();

    void validate(String templatePath, Map<String, Object> content);

}
