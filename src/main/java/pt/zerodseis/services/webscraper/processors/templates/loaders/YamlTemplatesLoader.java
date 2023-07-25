package pt.zerodseis.services.webscraper.processors.templates.loaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pt.zerodseis.services.webscraper.exceptions.LoadYamlTemplateException;
import pt.zerodseis.services.webscraper.processors.templates.TemplateType;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateDocument;
import pt.zerodseis.services.webscraper.processors.templates.YamlTemplateConstants;
import pt.zerodseis.services.webscraper.processors.templates.validators.DefaultYamlTemplateValidator;

@Slf4j
@Component
public class YamlTemplatesLoader {

    private final Map<String, YamlTemplateDocument> templatesMap;

    public YamlTemplatesLoader(
            @Value("${processors.templates.loaders.yaml.directory}") String yamlTemplatesDirectory,
            ObjectMapper yamlObjectMapper,
            DefaultYamlTemplateValidator validator) throws IOException {

        this.templatesMap = new HashMap<>();

        Path path = Paths.get(yamlTemplatesDirectory);

        if (isDirectoryAndExists(path)) {
            List<Path> templates = getTemplates(path);

            for (Path template : templates) {
                Map<String, Object> content = readTemplateContent(yamlObjectMapper, template);
                validator.validate(template.toString(), content);

                String type = (String) content.get(YamlTemplateConstants.TYPE_TAG);
                String site = (String) content.get(YamlTemplateConstants.SITE_TAG);
                templatesMap.put(site, new YamlTemplateDocument(TemplateType.fromName(type), content));
            }
        } else {
            log.warn(
                    "Processor templates directory doesn't exist. Check the path value defined in {} property",
                    yamlTemplatesDirectory);
        }
    }

    private Map<String, Object> readTemplateContent(ObjectMapper mapper, Path path) {
        try {
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
            };

            return mapper.readValue(path.toFile(), typeRef);
        } catch (IOException e) {
            throw new LoadYamlTemplateException(
                    String.format("Could not read content of template file [%s]",
                            path),
                    e);
        }
    }

    private List<Path> getTemplates(Path path) throws IOException {
        try (Stream<Path> paths = Files.walk(path)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(f -> {
                        String fileName = f.getFileName().toString().toLowerCase();
                        return fileName.endsWith(".yml") || fileName.equals(".yaml");
                    })
                    .toList();
        }
    }

    private boolean isDirectoryAndExists(Path path) {
        boolean exists = Files.exists(path);
        boolean isDirectory = Files.isDirectory(path);
        return exists && isDirectory;
    }
}
