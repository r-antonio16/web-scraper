package pt.zerodseis.services.webscraper.processors.templates.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.processors.templates.validators.DefaultYamlTemplateValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class YamlTemplatesLoaderTest {

    @Test
    public void Should_Load_Yaml_Templates_When_YamlTemplatesLoader_Is_Instantiated() throws IOException {
        String templatesDir = "src/test/resources/templates";
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        DefaultYamlTemplateValidator validator = mock(DefaultYamlTemplateValidator.class);
        List<Path> templates = getTemplates(Paths.get(templatesDir));

        new YamlTemplatesLoader(templatesDir, objectMapper, validator);

        for (Path template : templates) {
            verify(validator).validate(eq(template.toString()), any());
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
}
