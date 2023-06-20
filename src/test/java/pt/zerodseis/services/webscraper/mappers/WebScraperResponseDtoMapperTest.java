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
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.web.models.WebScraperResponseDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class WebScraperResponseDtoMapperTest {

    @Autowired
    private WebScraperResponseDtoMapper mapper;

    @TestConfiguration
    public static class Config {

        @Bean
        public WebScraperResponseDtoMapper webScraperResponseDtoMapper() {
            return Mappers.getMapper(WebScraperResponseDtoMapper.class);
        }

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
    public void should_MapNonNullWebScraperResponseToWebScraperResponseDto_When_toDto_IsCalled() {
        WebScraperResponse response = MappersTestsHelper.getWebScraperResponse();

        WebScraperResponseDto dto = mapper.toDto(response);

        assertNotNull(dto);
        assertEquals(response.request().id(), dto.requestId());
        assertEquals(response.content(), dto.content());
        assertEquals(response.statusCode(), dto.statusCode());
        assertEquals(response.siteScrapStatus(), dto.siteScrapStatus());
    }

    @Test
    public void should_MapNullWebScraperResponseToNull_When_toDto_IsCalled() {
        WebScraperResponseDto dto = mapper.toDto(null);

        assertNull(dto);
    }

    @Test
    public void should_MapNonNullWebScraperResponseDtoToWebScraperResponse_When_fromDto_IsCalled() {
        WebScraperResponseDto dto = MappersTestsHelper.getWebScraperResponseDto();

        WebScraperResponse response = mapper.fromDto(dto);

        assertNotNull(response);
        assertNotNull(response.request());
        assertEquals(dto.requestId(), response.request().id());
        assertEquals(dto.content(), response.content());
        assertEquals(dto.statusCode(), response.statusCode());
        assertEquals(dto.siteScrapStatus(), response.siteScrapStatus());
    }

    @Test
    public void should_MapNullWebScraperResponseDtoToNull_When_fromDto_IsCalled() {
        WebScraperResponse response = mapper.fromDto(null);

        assertNull(response);
    }
}
