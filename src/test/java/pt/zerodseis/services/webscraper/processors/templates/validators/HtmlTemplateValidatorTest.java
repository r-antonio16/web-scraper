package pt.zerodseis.services.webscraper.processors.templates.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.exceptions.YamlTemplateValidationException;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
public class HtmlTemplateValidatorTest {

    private final HtmlTemplateValidator validator = new HtmlTemplateValidator();

    @Test
    public void Should_Not_Throw_Exception_When_Template_Has_All_Required_Tags() {
        Map<String, Object> template = getTemplateMock(true, true, true);
        assertAll(() -> validator.validate("test-template", template));
    }

    @Test
    public void Should_Throw_Exception_When_Template_Map_Tag_Is_Empty() {
        Map<String, Object> template = getTemplateMock(false, false, false);

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [map tag is missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Loop_Is_Empty() {
        Map<String, Object> template = getTemplateMock(true, false, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);

        rootMapTag.get(0).put(YamlTemplateConstants.LOOP_TAG, List.of());

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > loop tag - map/loop or " +
                        "condition is missing, root map > 0th  > loop tag - expression tag is missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Loop_Misses_Expression() {
        Map<String, Object> template = getTemplateMock(true, false, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> loopTag = getLoopTagMock();

        loopTag.removeIf(map -> map.containsKey(YamlTemplateConstants.EXPRESSION_TAG));

        rootMapTag.get(0).put(YamlTemplateConstants.LOOP_TAG, loopTag);


        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > loop tag - expression tag is " +
                        "missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Loop_Expression_Does_Not_Match_Regex() {
        Map<String, Object> template = getTemplateMock(true, false, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> loopTag = getLoopTagMock();

        for (Map<String, Object> subTag : loopTag) {
            if (subTag.containsKey(YamlTemplateConstants.EXPRESSION_TAG)) {
                subTag.put(YamlTemplateConstants.EXPRESSION_TAG, "non-sense");
            }
        }

        rootMapTag.get(0).put(YamlTemplateConstants.LOOP_TAG, loopTag);


        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > loop tag - expression tag is " +
                        "invalid]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Loop_Expression_Misses_Nested_Tag() {
        Map<String, Object> template = getTemplateMock(true, false, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> loopTag = getLoopTagMock();

        loopTag.removeIf(map -> map.containsKey(YamlTemplateConstants.MAP_TAG));

        rootMapTag.get(0).put(YamlTemplateConstants.LOOP_TAG, loopTag);


        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > loop tag - map/loop or " +
                        "condition is missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Loop_Nested_Tag_Misses_Tag() {
        Map<String, Object> template = getTemplateMock(true, false, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> loopTag = getLoopTagMock();
        List<Map<String, String>> mapTag = getMapTagMock();

        mapTag.removeIf(map -> map.containsKey(YamlTemplateConstants.SOURCE_TAG));

        for (Map<String, Object> subTag : loopTag) {
            if (subTag.containsKey(YamlTemplateConstants.MAP_TAG)) {
                subTag.put(YamlTemplateConstants.MAP_TAG, mapTag);
            }
        }

        rootMapTag.get(0).put(YamlTemplateConstants.LOOP_TAG, loopTag);


        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > loop > map tag - source tag is " +
                        "missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Condition_Is_Empty() {
        Map<String, Object> template = getTemplateMock(false, true, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);

        rootMapTag.get(0).put(YamlTemplateConstants.CONDITION_TAG, List.of());

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > condition tag - if tag is " +
                        "missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Condition_If_Tag_Misses_Expression() {
        Map<String, Object> template = getTemplateMock(false, true, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> ifCondition = getConditionTagMock(false);

        rootMapTag.get(0)
                  .put(YamlTemplateConstants.CONDITION_TAG, List.of(Map.of(YamlTemplateConstants.IF_TAG, ifCondition)));

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > condition > if tag - expression" +
                        " tag is missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Condition_If_Tag_Misses_NestedTags() {
        Map<String, Object> template = getTemplateMock(false, true, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> ifCondition = new LinkedList<>();
        ifCondition.add(Map.of(YamlTemplateConstants.EXPRESSION_TAG, "expression"));

        rootMapTag.get(0)
                  .put(YamlTemplateConstants.CONDITION_TAG, List.of(Map.of(YamlTemplateConstants.IF_TAG, ifCondition)));

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > condition > if tag - nested tag" +
                        " is missing]",
                throwable.getLocalizedMessage());
    }

    @ParameterizedTest
    @CsvSource({YamlTemplateConstants.ELSE_IF_TAG + ",if", YamlTemplateConstants.ELSE_TAG + ",if or else-if"})
    public void Should_Throw_Exception_When_Template_Condition_Misses_If_Tag(String tag, String message) {
        Map<String, Object> template = getTemplateMock(false, true, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> conditionTag = getConditionTagMock(true);

        rootMapTag.get(0)
                  .put(YamlTemplateConstants.CONDITION_TAG, List.of(Map.of(tag,
                          conditionTag)));

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > condition > " + tag + " tag - " +
                        "matching " + message + " tag is missing, root map > 0th  > condition tag - if tag is missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Condition_If_Else_Tag_Misses_Expression() {
        Map<String, Object> template = getTemplateMock(false, true, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> ifCondition = getConditionTagMock(true);
        List<Map<String, Object>> ifElseCondition = getConditionTagMock(false);

        rootMapTag.get(0)
                  .put(YamlTemplateConstants.CONDITION_TAG, List.of(
                                  Map.of(YamlTemplateConstants.IF_TAG, ifCondition),
                                  Map.of(YamlTemplateConstants.ELSE_IF_TAG, ifElseCondition)
                          )
                  );

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > condition > else-if tag - " +
                        "expression tag is missing]",
                throwable.getLocalizedMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {YamlTemplateConstants.ELSE_IF_TAG, YamlTemplateConstants.ELSE_TAG})
    public void Should_Throw_Exception_When_Template_Condition_Sub_Tag_Misses_NestedTags(String tag) {
        Map<String, Object> template = getTemplateMock(false, true, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> ifCondition = getConditionTagMock(true);
        List<Map<String, Object>> conditionTag = new LinkedList<>();
        conditionTag.add(Map.of(YamlTemplateConstants.EXPRESSION_TAG, "expression"));

        rootMapTag.get(0)
                  .put(YamlTemplateConstants.CONDITION_TAG, List.of(
                                  Map.of(YamlTemplateConstants.IF_TAG, ifCondition),
                                  Map.of(tag, conditionTag)
                          )
                  );

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > condition > " + tag + " tag - " +
                        "nested tag is missing]",
                throwable.getLocalizedMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {YamlTemplateConstants.IF_TAG, YamlTemplateConstants.ELSE_IF_TAG,
            YamlTemplateConstants.ELSE_TAG})
    public void Should_Throw_Exception_When_Template_Condition_Sub_Tag_NestedTag_Misses_Tag(String tag) {
        Map<String, Object> template = getTemplateMock(false, true, false);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> conditionTag = (List<Map<String, Object>>) rootMapTag.get(0)
                                                                                       .get(YamlTemplateConstants.CONDITION_TAG);

        for (Map<String, Object> conditionSubTag : conditionTag) {
            if (conditionSubTag.containsKey(tag)) {
                List<Map<String, Object>> subTag =
                        (List<Map<String, Object>>) conditionSubTag.get(tag);

                subTag.stream()
                      .filter(e -> e.containsKey(YamlTemplateConstants.MAP_TAG))
                      .map(e -> e.get(YamlTemplateConstants.MAP_TAG))
                      .map(List.class::cast)
                      .findFirst()
                      .ifPresent(list -> {
                          list.remove(1);
                      });

            }
        }

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > condition > " + tag + " > map " +
                        "tag - target tag is missing]",
                throwable.getLocalizedMessage());
    }

    @Test
    public void Should_Throw_Exception_When_Template_Map_Is_Empty() {
        Map<String, Object> template = getTemplateMock(false, false, true);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);

        rootMapTag.get(0).put(YamlTemplateConstants.MAP_TAG, List.of());

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > map tag - source tag is " +
                        "missing, root map > 0th  > map tag - target tag is missing]",
                throwable.getLocalizedMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {YamlTemplateConstants.SOURCE_TAG, YamlTemplateConstants.TARGET_TAG})
    public void Should_Throw_Exception_When_Template_Map_Misses_Tag(String tag) {
        Map<String, Object> template = getTemplateMock(false, false, true);

        List<Map<String, Object>> rootMapTag = (List<Map<String, Object>>) template.get(YamlTemplateConstants.MAP_TAG);
        List<Map<String, Object>> mapTag = (List<Map<String, Object>>) rootMapTag.get(0)
                                                                                 .get(YamlTemplateConstants.MAP_TAG);

        mapTag.removeIf(map -> map.containsKey(tag));

        Throwable throwable = assertThrows(YamlTemplateValidationException.class,
                () -> validator.validate("test-template", template));

        assertEquals("Template test-template is not valid. Errors: [root map > 0th  > map tag - " + tag + " tag is " +
                        "missing]",
                throwable.getLocalizedMessage());
    }

    private Map<String, Object> getTemplateMock(boolean includeLoopTag, boolean includeConditionTag,
                                                boolean includeMapTag) {
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> rootMapTag = new LinkedList<>();

        if (includeLoopTag) {
            rootMapTag.add(new HashMap<>(Map.of(YamlTemplateConstants.LOOP_TAG, getLoopTagMock())));
        }

        if (includeConditionTag) {
            rootMapTag.add(
                    new HashMap<>(
                            Map.of(YamlTemplateConstants.CONDITION_TAG,
                                    List.of(
                                            Map.of(YamlTemplateConstants.IF_TAG, getConditionTagMock(true)),
                                            Map.of(YamlTemplateConstants.ELSE_IF_TAG, getConditionTagMock(true)),
                                            Map.of(YamlTemplateConstants.ELSE_TAG, getConditionTagMock(false))
                                    )
                            )
                    )
            );
        }

        if (includeMapTag) {
            rootMapTag.add(new HashMap<>(Map.of(YamlTemplateConstants.MAP_TAG, getMapTagMock())));
        }

        template.put(YamlTemplateConstants.MAP_TAG, rootMapTag);

        return template;
    }

    private List<Map<String, String>> getMapTagMock() {
        List<Map<String, String>> mapTag = new LinkedList<>();

        Map<String, String> loopMapSource = new HashMap<>();
        Map<String, String> loopMapTarget = new HashMap<>();

        loopMapSource.put(YamlTemplateConstants.SOURCE_TAG, "item.name");
        loopMapTarget.put(YamlTemplateConstants.TARGET_TAG, "name");

        mapTag.add(loopMapSource);
        mapTag.add(loopMapTarget);

        return mapTag;
    }

    private List<Map<String, Object>> getLoopTagMock() {
        List<Map<String, Object>> loopTag = new LinkedList<>();

        loopTag.add(new HashMap<>(
                        Map.of(YamlTemplateConstants.EXPRESSION_TAG, "item from $('.div > div')")
                )
        );

        loopTag.add(new HashMap<>(Map.of(YamlTemplateConstants.MAP_TAG, getMapTagMock())));

        return loopTag;
    }

    private List<Map<String, Object>> getConditionTagMock(boolean withExpression) {
        List<Map<String, Object>> condTag = new LinkedList<>();

        if (withExpression) {
            condTag.add(
                    new HashMap<>(
                            Map.of(YamlTemplateConstants.EXPRESSION_TAG, "$('.div > div').text != null")
                    )
            );
        }

        condTag.add(
                new HashMap<>(
                        Map.of(YamlTemplateConstants.MAP_TAG, getMapTagMock()
                        )
                )
        );

        return condTag;
    }
}
