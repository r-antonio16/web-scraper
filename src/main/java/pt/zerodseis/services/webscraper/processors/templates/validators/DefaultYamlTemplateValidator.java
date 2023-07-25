package pt.zerodseis.services.webscraper.processors.templates.validators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pt.zerodseis.services.webscraper.exceptions.UnsupportedYamlTemplateException;
import pt.zerodseis.services.webscraper.exceptions.YamlTemplateValidationException;
import pt.zerodseis.services.webscraper.processors.templates.TemplateType;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateConstants;

@Component
public class DefaultYamlTemplateValidator {

    private final Map<TemplateType, YamlTemplateTypeValidator> templateTypeValidators;

    public DefaultYamlTemplateValidator(
            List<YamlTemplateTypeValidator> templateTypeValidators) {
        this.templateTypeValidators = new HashMap<>();

        Optional.ofNullable(templateTypeValidators)
                .orElseGet(Collections::emptyList).forEach(validator ->
                        this.templateTypeValidators.put(validator.getType(), validator)
                );
    }

    public void validate(String templatePath, Map<String, Object> content) {
        List<String> errors = new ArrayList<>();

        if (!content.containsKey(YamlTemplateConstants.TYPE_TAG)
                || !StringUtils.hasText(
                (String) content.get(YamlTemplateConstants.TYPE_TAG))) {
            errors.add("type tag is missing");
        }

        if (!content.containsKey(YamlTemplateConstants.SITE_TAG)
                || !StringUtils.hasText(
                (String) content.get(YamlTemplateConstants.SITE_TAG))) {
            errors.add("site tag is missing");
        }

        if (!errors.isEmpty()) {
            throw new YamlTemplateValidationException(templatePath, errors);
        }

        TemplateType type =
                Optional.ofNullable(content.get(YamlTemplateConstants.TYPE_TAG))
                        .map(String.class::cast)
                        .map(String::toLowerCase)
                        .map(TemplateType::fromName)
                        .orElse(null);

        if (!templateTypeValidators.containsKey(type)) {
            throw new UnsupportedYamlTemplateException("type value is not supported");
        }

        YamlTemplateTypeValidator typeValidator = templateTypeValidators.get(type);

        typeValidator.validate(templatePath, content);
    }
}
