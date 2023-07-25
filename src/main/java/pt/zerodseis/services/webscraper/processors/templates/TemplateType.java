package pt.zerodseis.services.webscraper.processors.templates;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplateType {
    JSON("json"),
    HTML("html");

    private final String name;

    public static TemplateType fromName(String name) {
        for (TemplateType templateType : TemplateType.values()) {
            if (templateType.getName().equals(name)) {
                return templateType;
            }
        }

        return null;
    }
}
