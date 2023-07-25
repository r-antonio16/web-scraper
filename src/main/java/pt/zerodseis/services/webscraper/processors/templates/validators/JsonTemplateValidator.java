package pt.zerodseis.services.webscraper.processors.templates.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import pt.zerodseis.services.webscraper.exceptions.YamlTemplateValidationException;
import pt.zerodseis.services.webscraper.processors.templates.TemplateType;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateConstants;

@Component
public class JsonTemplateValidator implements YamlTemplateTypeValidator {

    @Override
    public TemplateType getType() {
        return TemplateType.JSON;
    }

    @Override
    public void validate(String templatePath, Map<String, Object> content) {
        List<String> errors = new ArrayList<>();

        if (!content.containsKey(YamlTemplateConstants.SINGLE_TAG)
                && !content.containsKey(YamlTemplateConstants.ARRAY_TAG)) {
            errors.add("single or array tag is missing");
        } else {
            Map<String, Object> mapContent =
                    (Map<String, Object>) (content.containsKey(YamlTemplateConstants.SINGLE_TAG) ?
                    content.get(YamlTemplateConstants.SINGLE_TAG) : content.get(YamlTemplateConstants.ARRAY_TAG));

            if (!mapContent.containsKey(YamlTemplateConstants.MAP_TAG)
                    || CollectionUtils.isEmpty((Collection<?>) mapContent.get(YamlTemplateConstants.MAP_TAG))) {
                errors.add("map tag is missing");
            } else {

                List<Map<String, String>> mappings = (List<Map<String, String>>) mapContent.get(
                        YamlTemplateConstants.MAP_TAG);

                for (int i = 0; i < mappings.size(); i++) {
                    Map<String, String> mapping = mappings.get(i);

                    if (!mapping.containsKey(YamlTemplateConstants.SOURCE_TAG)
                            || !StringUtils.hasText(
                            mapping.get(YamlTemplateConstants.SOURCE_TAG))) {
                        errors.add(String.format("map tag %d - source property is missing", i + 1));
                    }

                    if (!mapping.containsKey(YamlTemplateConstants.TARGET_TAG)
                            || !StringUtils.hasText(
                            mapping.get(YamlTemplateConstants.TARGET_TAG))) {
                        errors.add(String.format("map tag %d - target property is missing", i + 1));
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new YamlTemplateValidationException(templatePath, errors);
        }
    }
}
