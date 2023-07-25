package pt.zerodseis.services.webscraper.processors.templates;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YamlTemplateConstants {
    public static final String TYPE_TAG = "type";
    public static final String SITE_TAG = "site";
    public static final String MAP_TAG = "map";
    public static final String SINGLE_TAG = "single";
    public static final String ARRAY_TAG = "array";
    public static final String SOURCE_TAG = "source";
    public static final String TARGET_TAG = "target";
    public static final String LOOP_TAG = "loop";
    public static final String EXPRESSION_TAG = "expression";
    public static final String CONDITION_TAG = "condition";
    public static final String IF_TAG = "if";
    public static final String ELSE_IF_TAG = "else-if";
    public static final String ELSE_TAG = "else";
    public static final Pattern LOOP_EXPRESSION_PATTERN = Pattern.compile("^\\w+( +from +\\$\\('.*'\\))*");
}
