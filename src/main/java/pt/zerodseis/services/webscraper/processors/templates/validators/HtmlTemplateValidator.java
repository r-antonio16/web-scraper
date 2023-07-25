package pt.zerodseis.services.webscraper.processors.templates.validators;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import pt.zerodseis.services.webscraper.exceptions.YamlTemplateValidationException;
import pt.zerodseis.services.webscraper.processors.templates.TemplateType;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class HtmlTemplateValidator implements YamlTemplateTypeValidator {

    private final static List<String> NESTED_TAGS = List.of(YamlTemplateConstants.LOOP_TAG,
            YamlTemplateConstants.CONDITION_TAG, YamlTemplateConstants.MAP_TAG);

    @Override
    public TemplateType getType() {
        return TemplateType.HTML;
    }

    @Override
    public void validate(String templatePath, Map<String, Object> content) {
        List<String> errors = new ArrayList<>();

        if (!content.containsKey(YamlTemplateConstants.MAP_TAG)
                || CollectionUtils.isEmpty((Collection<?>) content.get(YamlTemplateConstants.MAP_TAG))) {
            errors.add("map tag is missing");
        } else {

            List<Map<String, Object>> tags = (List<Map<String, Object>>) content.get(YamlTemplateConstants.MAP_TAG);

            for (int i = 0; i < tags.size(); i++) {
                validateByTagType(errors, tags.get(i), "root map > " + ordinal(i) + " ");
            }
        }

        if (!errors.isEmpty()) {
            throw new YamlTemplateValidationException(templatePath, errors);
        }
    }

    private void validateByTagType(List<String> errors, Map<String, Object> tags, String tagsPath) {

        for (Entry<String, Object> tag : tags.entrySet()) {
            switch (tag.getKey()) {
                case YamlTemplateConstants.MAP_TAG -> validateMapTag(errors,
                        (List<Map<String, Object>>) tag.getValue(),
                        tagsPath + " > " + YamlTemplateConstants.MAP_TAG);
                case YamlTemplateConstants.LOOP_TAG -> validateLoopTag(errors,
                        (List<Map<String, Object>>) tag.getValue(),
                        tagsPath + " > " + YamlTemplateConstants.LOOP_TAG);
                case YamlTemplateConstants.CONDITION_TAG -> validateConditionTag(errors,
                        (List<Map<String, Object>>) tag.getValue(),
                        tagsPath + " > " + YamlTemplateConstants.CONDITION_TAG);
            }
        }
    }

    private void validateMapTag(List<String> errors, List<Map<String, Object>> content, String tagsPath) {
        Map<String, String> tags = new HashMap<>();
        tags.put(YamlTemplateConstants.SOURCE_TAG, "");
        tags.put(YamlTemplateConstants.TARGET_TAG, "");

        content.forEach(entry -> {
            for (Entry<String, Object> tag : entry.entrySet()) {
                String tagType = tag.getKey();
                String tagValue = (String) tag.getValue();

                if (YamlTemplateConstants.SOURCE_TAG.equals(tagType)) {
                    tags.put(YamlTemplateConstants.SOURCE_TAG, tagValue);
                }

                if (YamlTemplateConstants.TARGET_TAG.equals(tagType)) {
                    tags.put(YamlTemplateConstants.TARGET_TAG, tagValue);
                }
            }
        });

        String source = tags.getOrDefault(YamlTemplateConstants.SOURCE_TAG, "");
        String target = tags.getOrDefault(YamlTemplateConstants.TARGET_TAG, "");

        boolean hasSource = content.stream()
                                   .anyMatch(entry -> entry.containsKey(YamlTemplateConstants.SOURCE_TAG));
        boolean hasTarget = content.stream()
                                   .anyMatch(entry -> entry.containsKey(YamlTemplateConstants.TARGET_TAG));

        addErrorIfNotMet(errors, hasSource && StringUtils.hasText(source), tagsPath + " tag - source tag is missing");
        addErrorIfNotMet(errors, hasTarget && StringUtils.hasText(target), tagsPath + " tag - target tag is missing");
    }

    private void addErrorIfNotMet(List<String> errors, boolean condition, String errorMessage) {
        if (!condition) {
            errors.add(errorMessage);
        }
    }

    private void validateLoopTag(List<String> errors, List<Map<String, Object>> content, String tagsPath) {
        boolean hasExpression = false;
        boolean hasNestedTag = false;
        String expression = Strings.EMPTY;

        for (Map<String, Object> entry : content) {
            if (entry.containsKey(YamlTemplateConstants.EXPRESSION_TAG)) {
                hasExpression = true;
                expression = (String) entry.get(YamlTemplateConstants.EXPRESSION_TAG);
            }

            if (entry.keySet().stream()
                     .anyMatch(NESTED_TAGS::contains)) {
                hasNestedTag = true;
                validateByTagType(errors, entry, tagsPath);
            }
        }

        if (!hasNestedTag) {
            errors.add(tagsPath + " tag - map/loop or condition is missing");
        }

        if (!hasExpression || expression.isEmpty()) {
            errors.add(tagsPath + " tag - expression tag is missing");
        } else if (!YamlTemplateConstants.LOOP_EXPRESSION_PATTERN.matcher(expression).matches()) {
            errors.add(tagsPath + " tag - expression tag is invalid");
        }
    }

    private void validateConditionTag(List<String> errors, List<Map<String, Object>> content, String tagsPath) {
        boolean foundIf = false;
        boolean foundElseIf = false;
        boolean foundAnyIf = false;

        for (Map<String, Object> entry : content) {
            for (Entry<String, Object> tag : entry.entrySet()) {
                String tagType = tag.getKey();
                List<Map<String, Object>> tagValue = (List<Map<String, Object>>) tag.getValue();

                String expression = getExpressionTagValue(tagValue);

                if (YamlTemplateConstants.IF_TAG.equals(tagType)) {
                    foundIf = true;
                    foundAnyIf = true;

                    if (expression.isEmpty()) {
                        errors.add(tagsPath + " > if tag - expression tag is missing");
                    }

                    if (containsNoneNestedTag(tagValue)) {
                        errors.add(tagsPath + " > if tag - nested tag is missing");
                    }
                }

                if (YamlTemplateConstants.ELSE_IF_TAG.equals(tagType)) {
                    if (!foundIf) {
                        errors.add(tagsPath + " > else-if tag - matching if tag is missing");
                    }

                    if (expression.isEmpty()) {
                        errors.add(tagsPath + " > else-if tag - expression tag is missing");
                    }

                    if (containsNoneNestedTag(tagValue)) {
                        errors.add(tagsPath + " > else-if tag - nested tag is missing");
                    }

                    foundElseIf = true;
                }

                if (YamlTemplateConstants.ELSE_TAG.equals(tagType)) {
                    if (!foundIf && !foundElseIf) {
                        errors.add(tagsPath + " > else tag - matching if or else-if tag is missing");
                    }

                    foundIf = false;
                    foundElseIf = false;

                    if (containsNoneNestedTag(tagValue)) {
                        errors.add(tagsPath + " > else tag - nested tag is missing");
                    }
                }

                validateNestedTags(errors, tagValue, tagsPath + " > " + tagType);
            }
        }

        if (!foundAnyIf) {
            errors.add(tagsPath + " tag - if tag is missing");
        }
    }

    private void validateNestedTags(List<String> errors, List<Map<String, Object>> tagValue, String tagsPath) {
        tagValue.forEach(m ->
                m.entrySet().stream()
                 .filter(e -> NESTED_TAGS.contains(e.getKey()))
                 .findFirst()
                 .ifPresent(e -> validateByTagType(errors, m, tagsPath))
        );
    }

    private String getExpressionTagValue(List<Map<String, Object>> tagValue) {
        for (Map<String, Object> tagMap : tagValue) {
            if (tagMap.containsKey(YamlTemplateConstants.EXPRESSION_TAG)) {
                Object expression = tagMap.get(YamlTemplateConstants.EXPRESSION_TAG);
                if (expression instanceof String) {
                    return (String) expression;
                }
            }
        }
        return Strings.EMPTY;
    }

    private boolean containsNoneNestedTag(List<Map<String, Object>> tagValue) {
        return tagValue.stream()
                       .flatMap(m -> m.entrySet().stream())
                       .noneMatch(e -> NESTED_TAGS.contains(e.getKey()));
    }

    public static String ordinal(int i) {
        int mod100 = i % 100;
        int mod10 = i % 10;
        if (mod10 == 1 && mod100 != 11) {
            return i + "st";
        } else if (mod10 == 2 && mod100 != 12) {
            return i + "nd";
        } else if (mod10 == 3 && mod100 != 13) {
            return i + "rd";
        } else {
            return i + "th";
        }
    }
}
