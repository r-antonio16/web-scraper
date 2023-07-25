package pt.zerodseis.services.webscraper.processors.templates;

import java.util.Map;

public record YamlTemplateDocument(TemplateType type, Map<String, Object> document) {

}
