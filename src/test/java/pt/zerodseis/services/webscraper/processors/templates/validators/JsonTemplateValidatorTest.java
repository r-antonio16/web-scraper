package pt.zerodseis.services.webscraper.processors.templates.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.exceptions.YamlTemplateValidationException;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
public class JsonTemplateValidatorTest {

    private final JsonTemplateValidator validator = new JsonTemplateValidator();

    @Test
    public void Should_Not_Throw_Exception_When_Template_Has_All_Required_Tags() {
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> mapTag = new HashMap<>();
        Map<String, String> mapping1Content = new HashMap<>();

        mapping1Content.put(YamlTemplateConstants.SOURCE_TAG, "sourceField1");
        mapping1Content.put(YamlTemplateConstants.TARGET_TAG, "targetField1");
        Map<String, String> mapping2Content = new HashMap<>();

        mapping2Content.put(YamlTemplateConstants.SOURCE_TAG, "sourceField2");
        mapping2Content.put(YamlTemplateConstants.TARGET_TAG, "targetField2");

        mapTag.put(YamlTemplateConstants.MAP_TAG, List.of(mapping1Content, mapping2Content));
        template.put(YamlTemplateConstants.SINGLE_TAG, mapTag);

        assertAll(() -> validator.validate("test-template", template));
    }

    @Test
    public void Should_Throw_Exception_When_Template_Misses_Single_Or_Array_Tag() {
        Map<String, Object> template = new HashMap<>();

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [single or array tag is missing]", throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Misses_Map_Tag() {
        Map<String, Object> template = new HashMap<>();

        template.put(YamlTemplateConstants.SINGLE_TAG, Map.of());

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [map tag is missing]", throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Map_Tag_Is_Empty() {
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> mapTag = new HashMap<>();

        mapTag.put(YamlTemplateConstants.MAP_TAG, List.of());
        template.put(YamlTemplateConstants.SINGLE_TAG, mapTag);

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [map tag is missing]", throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Misses_Source_Tag() {
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> mapTag = new HashMap<>();
        Map<String, String> mapping1Content = new HashMap<>();

        mapping1Content.put(YamlTemplateConstants.TARGET_TAG, "targetField1");

        mapTag.put(YamlTemplateConstants.MAP_TAG, List.of(mapping1Content));
        template.put(YamlTemplateConstants.SINGLE_TAG, mapTag);

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [map tag 1 - source property is missing]", throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Misses_Target_Tag() {
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> mapTag = new HashMap<>();
        Map<String, String> mapping1Content = new HashMap<>();

        mapping1Content.put(YamlTemplateConstants.SOURCE_TAG, "sourceField1");

        mapTag.put(YamlTemplateConstants.MAP_TAG, List.of(mapping1Content));
        template.put(YamlTemplateConstants.SINGLE_TAG, mapTag);

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [map tag 1 - target property is missing]", throwable.getLocalizedMessage());
    }
}
