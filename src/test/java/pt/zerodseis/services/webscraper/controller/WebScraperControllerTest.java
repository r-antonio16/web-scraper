package pt.zerodseis.services.webscraper.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pt.zerodseis.services.webscraper.exceptions.ConnectionProviderRuntimeException;
import pt.zerodseis.services.webscraper.exceptions.ReadUserAgentJsonException;
import pt.zerodseis.services.webscraper.mappers.MappersTestsHelper;
import pt.zerodseis.services.webscraper.service.WebScraperService;
import pt.zerodseis.services.webscraper.web.controller.WebScraperController;
import pt.zerodseis.services.webscraper.web.models.WebScraperRequestDto;
import pt.zerodseis.services.webscraper.web.models.WebScraperResponseDto;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = WebScraperController.class)
public class WebScraperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebScraperService service;

    @Test
    public void should_SiteEndpointReturn200_When_ValidRequest() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDto();
        WebScraperResponseDto responseDto = MappersTestsHelper.getWebScraperResponseDto();
        when(service.scrapSite(requestDto)).thenReturn(responseDto);
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @ParameterizedTest
    @MethodSource("getScrapSite400RequestObjectsToTest")
    public void should_SiteEndpointReturn400_When_InvalidRequestIsPassed(WebScraperRequestDto requestDto,
            String error) throws Exception {

        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['" + error + "']}"));
    }

    static Stream<Arguments> getScrapSite400RequestObjectsToTest() {
        return Stream.of(
                Arguments.of(null,
                        "Required request body is missing"),
                Arguments.of(MappersTestsHelper.getWebScraperRequestDtoWithNullId(),
                        "id : must not be null"),
                Arguments.of(MappersTestsHelper.getWebScraperRequestDtoWithNullUrl(),
                        "url : must not be blank"),
                Arguments.of(MappersTestsHelper.getWebScraperRequestDtoWithBlankUrl(),
                        "url : must not be blank','url : must be a valid URL"),
                Arguments.of(
                        MappersTestsHelper.getWebScraperRequestDtoWithCookieNullName(),
                        "cookies[1].name : must not be blank"),
                Arguments.of(
                        MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankName(),
                        "cookies[1].name : must not be blank"),
                Arguments.of(
                        MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankValue(),
                        "cookies[1].value : must not be blank"),
                Arguments.of(
                        MappersTestsHelper.getWebScraperRequestDtoWithCookieNullValue(),
                        "cookies[1].value : must not be blank")
        );
    }

    @ParameterizedTest
    @MethodSource("getScrapSites200RequestObjectsToTest")
    public void should_SitesEndpointReturn200_When_ValidRequestIsPassed(
            List<WebScraperRequestDto> requestDto, List<WebScraperResponseDto> responseDto)
            throws Exception {
        reset(service);
        when(service.scrapSites(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    static Stream<Arguments> getScrapSites200RequestObjectsToTest() {
        return Stream.of(
                Arguments.of(
                        MappersTestsHelper.getWebScraperRequestListDto(5),
                        MappersTestsHelper.getWebScraperResponseListDto(
                                5)),
                Arguments.of(List.of(
                                MappersTestsHelper.getWebScraperRequestDtoWithNullCookies()),
                        MappersTestsHelper.getWebScraperResponseListDto(
                                1)),
                Arguments.of(List.of(
                                MappersTestsHelper.getWebScraperRequestDtoWithEmptyCookies()),
                        MappersTestsHelper.getWebScraperResponseListDto(
                                1))
        );
    }

    @ParameterizedTest
    @MethodSource("getScrapSites400RequestObjectsToTest")
    public void should_SitesEndpointReturn400_When_InvalidRequestIsPassed(
            List<WebScraperRequestDto> requestListDto, String error) throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                requestListDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['" + error + "']}"));
    }

    static Stream<Arguments> getScrapSites400RequestObjectsToTest() {
        return Stream.of(
                Arguments.of(null,
                        "Required request body is missing"),
                Arguments.of(Collections.emptyList(),
                        "scrapSites.requests : must not be empty"),
                Arguments.of(List.of(MappersTestsHelper.getWebScraperRequestDtoWithNullId()),
                        "scrapSites.requests[0].id : must not be null"),
                Arguments.of(List.of(MappersTestsHelper.getWebScraperRequestDtoWithNullUrl()),
                        "scrapSites.requests[0].url : must not be blank"),
                Arguments.of(List.of(MappersTestsHelper.getWebScraperRequestDtoWithBlankUrl()),
                        "scrapSites.requests[0].url : must not be blank','scrapSites.requests[0].url : must be a valid URL"),
                Arguments.of(
                        List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieNullName()),
                        "scrapSites.requests[0].cookies[1].name : must not be blank"),
                Arguments.of(
                        List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankName()),
                        "scrapSites.requests[0].cookies[1].name : must not be blank"),
                Arguments.of(
                        List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankValue()),
                        "scrapSites.requests[0].cookies[1].value : must not be blank"),
                Arguments.of(
                        List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieNullValue()),
                        "scrapSites.requests[0].cookies[1].value : must not be blank")
        );
    }

    @ParameterizedTest
    @MethodSource("getHandledExceptionsArgsToTest")
    public void should_SitesEndpointReturn500_When_ThrowHandledExceptions(
            Class<? extends Exception> ex, String message)
            throws Exception {
        when(service.scrapSites(anyList())).thenThrow(ex);
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDto())))
                        .contentType("application/json"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{'status':'INTERNAL_SERVER_ERROR',"
                        + "'message':'" + message + "','errors':[]}"));
    }

    static Stream<Arguments> getHandledExceptionsArgsToTest() {
        return Stream.of(
                Arguments.of(RuntimeException.class, "Unexpected Exception"),
                Arguments.of(ReadUserAgentJsonException.class, "Default User Agents Load Error"),
                Arguments.of(ConnectionProviderRuntimeException.class,
                        "Web Connection Provider Runtime Error")
        );
    }
}