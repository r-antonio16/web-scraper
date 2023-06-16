package pt.zerodseis.services.webscraper.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.mappers.HttpCookieDtoMapper;
import pt.zerodseis.services.webscraper.mappers.MappersTestsHelper;
import pt.zerodseis.services.webscraper.mappers.WebScraperRequestDtoMapper;
import pt.zerodseis.services.webscraper.mappers.WebScraperResponseDtoMapper;
import pt.zerodseis.services.webscraper.runners.WebScraperRunner;
import pt.zerodseis.services.webscraper.web.models.WebScraperRequestDto;
import pt.zerodseis.services.webscraper.web.models.WebScraperResponseDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class WebScraperServiceTest {

    @Mock
    private WebScraperRunner runnerMock;

    @Autowired
    private WebScraperRequestDtoMapper requestDtoMapper;

    @Autowired
    private WebScraperResponseDtoMapper responseDtoMapper;

    @TestConfiguration
    static class WebScrapServiceTestContextConfiguration {

        @Bean
        public WebScraperRequestDtoMapper webScraperRequestDtoMapper() {
            return Mappers.getMapper(WebScraperRequestDtoMapper.class);
        }

        @Bean
        public WebScraperResponseDtoMapper webScraperResponseDtoMapper() {
            return Mappers.getMapper(WebScraperResponseDtoMapper.class);
        }

        @Bean
        public HttpCookieDtoMapper httpCookieDtoMapper() {
            return Mappers.getMapper(HttpCookieDtoMapper.class);
        }
    }

    @Test
    public void should_ReturnWebScraperResponseDto_When_NonNullWebScraperRequestDtoIsPassedToScrapSite() {
        WebScraperService service = new WebScraperService(runnerMock, requestDtoMapper,
                responseDtoMapper);
        WebScraperRequestDto dto = MappersTestsHelper.getWebScraperRequestDto();
        WebScraperResponse response = MappersTestsHelper.getWebScraperResponse();

        when(runnerMock.scrapSite(any())).thenReturn(response);

        WebScraperResponseDto responseDto = service.scrapSite(dto);

        assertNotNull(responseDto);
        assertEquals(response.request().id(), responseDto.requestId());
        assertEquals(response.content(), responseDto.content());
        assertEquals(response.statusCode(), responseDto.statusCode());
        assertEquals(response.scrapTaskStatus(), responseDto.scrapTaskStatus());
    }

    @Test
    public void should_ReturnNull_When_NullWebScraperRequestDtoIsPassedToScrapSite() {
        WebScraperService service = new WebScraperService(runnerMock, requestDtoMapper,
                responseDtoMapper);
        reset(runnerMock);
        when(runnerMock.scrapSite(null)).thenReturn(null);

        WebScraperResponseDto responseDto = service.scrapSite(null);

        assertNull(responseDto);
    }

    @Test
    public void should_ServiceScrapSiteThrowException_When_RunnerScrapSiteThrowsRuntimeException() {
        WebScraperService service = new WebScraperService(runnerMock, requestDtoMapper,
                responseDtoMapper);
        WebScraperRequestDto dto = MappersTestsHelper.getWebScraperRequestDto();
        reset(runnerMock);
        when(runnerMock.scrapSite(any())).thenThrow(RuntimeException.class);

        assertThrows(Exception.class, () -> service.scrapSite(dto));
    }

    @Test
    public void should_ReturnWebScraperResponseDtoList_When_NonNullWebScraperRequestDtoIsPassedToScrapSites() {
        List<WebScraperResponse> response = MappersTestsHelper.getWebScraperResponse(5);
        WebScraperService service = new WebScraperService(runnerMock, requestDtoMapper,
                responseDtoMapper);

        reset(runnerMock);
        when(runnerMock.scrapSites(any())).thenReturn(response);

        List<WebScraperResponseDto> responseListDto = service.scrapSites(
                List.of(MappersTestsHelper.getWebScraperRequestDto()));
        responseListDto.sort(Comparator.comparing(WebScraperResponseDto::requestId));

        assertNotNull(responseListDto);
        assertFalse(responseListDto.isEmpty());

        for (int i = 0; i < responseListDto.size(); i++) {
            assertEquals(response.get(i).request().id(), responseListDto.get(i).requestId());
            assertEquals(response.get(i).content(), responseListDto.get(i).content());
            assertEquals(response.get(i).statusCode(), responseListDto.get(i).statusCode());
            assertEquals(response.get(i).scrapTaskStatus(),
                    responseListDto.get(i).scrapTaskStatus());
        }
    }

    @Test
    public void should_ReturnEmptyList_When_NullWebScraperRequestDtoListIsPassedToScrapSites() {
        WebScraperService service = new WebScraperService(runnerMock, requestDtoMapper,
                responseDtoMapper);
        reset(runnerMock);
        when(runnerMock.scrapSite(null)).thenReturn(null);

        List<WebScraperResponseDto> responseListDto = service.scrapSites(null);

        assertNotNull(responseListDto);
        assertTrue(responseListDto.isEmpty());
    }

    @Test
    public void should_ReturnEmptyList_When_WebScraperRequestDtoListIsPassedToScrapSites() {
        WebScraperService service = new WebScraperService(runnerMock, requestDtoMapper,
                responseDtoMapper);
        reset(runnerMock);
        when(runnerMock.scrapSites(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<WebScraperResponseDto> responseListDto = service.scrapSites(Collections.emptyList());

        assertNotNull(responseListDto);
        assertTrue(responseListDto.isEmpty());
    }

    @Test
    public void should_ServiceScrapSitesThrowException_When_RunnerScrapSitesThrowsRuntimeException() {
        WebScraperService service = new WebScraperService(runnerMock, requestDtoMapper,
                responseDtoMapper);
        reset(runnerMock);
        when(runnerMock.scrapSites(any())).thenThrow(RuntimeException.class);

        assertThrows(Exception.class, () -> service.scrapSites(Collections.emptyList()));
    }
}
