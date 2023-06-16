package pt.zerodseis.services.webscraper.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.web.models.WebScraperRequestDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class WebScraperRequestDtoMapperTest {

    @Autowired
    private WebScraperRequestDtoMapper mapper;

    @TestConfiguration
    public static class Config {

        @Bean
        public WebScraperRequestDtoMapper webScraperRequestDtoMapper() {
            return Mappers.getMapper(WebScraperRequestDtoMapper.class);
        }

        @Bean
        public HttpCookieDtoMapper httpCookieDtoMapper() {
            return Mappers.getMapper(HttpCookieDtoMapper.class);
        }
    }

    @Test
    public void should_MapNonNullWebScraperRequestToWebScraperRequestDto_When_toDto_IsCalled() {
        WebScraperRequest request = MappersTestsHelper.getWebScraperRequest();

        WebScraperRequestDto dto = mapper.toDto(request);

        assertNotNull(dto);
        assertNotNull(request);
        assertEquals(request.id(), dto.id());
        assertEquals(request.url().toString(), dto.url());
        assertEquals(request.cookies()[0].getName(), dto.cookies().get(0).name());
        assertEquals(request.cookies()[0].getValue(), dto.cookies().get(0).value());
        assertEquals(request.userAgent(), dto.userAgent());
    }

    @Test
    public void should_MapNullWebScraperRequestToNull_When_toDto_IsCalled() {
        WebScraperRequestDto dto = mapper.toDto(null);

        assertNull(dto);
    }

    @Test
    public void should_MapNonNulWebScraperRequestDtoToWebScraperRequest_When_fromDto_IsCalled() {
        WebScraperRequestDto dto = MappersTestsHelper.getWebScraperRequestDto();

        WebScraperRequest request = mapper.fromDto(dto);

        assertNotNull(request);
        assertEquals(dto.id(), request.id());
        assertEquals(dto.url(), request.url().toString());
        assertEquals(dto.cookies().get(0).name(), request.cookies()[0].getName());
        assertEquals(dto.cookies().get(0).value(), request.cookies()[0].getValue());
        assertEquals(dto.userAgent(), request.userAgent());
    }

    @Test
    public void should_MapNullWebScraperRequestDtoToNull_When_fromDto_IsCalled() {
        WebScraperRequest request = mapper.fromDto(null);

        assertNull(request);
    }
}
