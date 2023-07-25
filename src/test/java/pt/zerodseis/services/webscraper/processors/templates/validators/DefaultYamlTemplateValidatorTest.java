package pt.zerodseis.services.webscraper.processors.templates.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.exceptions.UnsupportedYamlTemplateException;
import pt.zerodseis.services.webscraper.exceptions.YamlTemplateValidationException;
import pt.zerodseis.services.webscraper.processors.templates.TemplateType;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class DefaultYamlTemplateValidatorTest {

    private DefaultYamlTemplateValidator validator;

    @BeforeEach
    public void init() {
        JsonTemplateValidator jsonValidator = mock(JsonTemplateValidator.class);
        when(jsonValidator.getType()).thenReturn(TemplateType.JSON);

        validator = new DefaultYamlTemplateValidator(List.of(jsonValidator));
    }

    @Test
    public void Should_Not_Throw_Exception_When_Template_Has_All_Required_Tags() {
        Map<String, Object> template = new HashMap<>();
        template.put(YamlTemplateConstants.TYPE_TAG, TemplateType.JSON.getName());
        template.put(YamlTemplateConstants.SITE_TAG, "www.test.com");

        assertAll(() -> validator.validate("test-template", template));
    }

    @Test
    public void Should_Throw_Exception_When_Template_Misses_Type_Tag() {
        Map<String, Object> template = new HashMap<>();
        template.put(YamlTemplateConstants.SITE_TAG, "www.test.com");

        assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));
    }

    @Test
    public void Should_Throw_Exception_When_Template_Misses_Site_Tag() {
        Map<String, Object> template = new HashMap<>();
        template.put(YamlTemplateConstants.TYPE_TAG, TemplateType.JSON.getName());

        assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));
    }

    @Test
    public void Should_Not_Throw_Exception_When_Template_Has_Unsupported_Type() {
        Map<String, Object> template = new HashMap<>();
        template.put(YamlTemplateConstants.TYPE_TAG, "new-type");
        template.put(YamlTemplateConstants.SITE_TAG, "www.test.com");

        assertThrows(UnsupportedYamlTemplateException.class,
                () -> validator.validate("test-template", template));
    }
}
