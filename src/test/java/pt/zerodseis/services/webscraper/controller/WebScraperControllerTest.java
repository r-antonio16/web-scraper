package pt.zerodseis.services.webscraper.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
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

    @Test
    public void should_SiteEndpointReturn400_When_NoContent() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/site")
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['Required request body is missing']}"));
    }

    @Test
    public void should_SiteEndpointReturn400_When_NullId() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDtoWithNullId();
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['id : must not be null']}"));


    }

    @Test
    public void should_SiteEndpointReturn400_When_NullUrl() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDtoWithNullUrl();
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['url : must not be blank']}"));
    }

    @Test
    public void should_SiteEndpointReturn400_When_BlankUrl() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDtoWithBlankUrl();
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['url : must not be blank', 'url : must be a valid URL']}"));
    }

    @Test
    public void should_SiteEndpointReturn400_When_NullCookieName() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDtoWithCookieNullName();
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['cookies[1].name : must not be blank']}"));
    }

    @Test
    public void should_SiteEndpointReturn400_When_BlankCookieName() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankName();
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['cookies[1].name : must not be blank']}"));
    }

    @Test
    public void should_SiteEndpointReturn400_When_NullCookieValue() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDtoWithCookieNullValue();
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['cookies[1].value : must not be blank']}"));
    }

    @Test
    public void should_SiteEndpointReturn400_When_BlankCookieValue() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankValue();
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['cookies[1].value : must not be blank']}"));
    }

    @Test
    public void should_SitesEndpointReturn200_When_ValidRequest() throws Exception {
        List<WebScraperRequestDto> requestDto = MappersTestsHelper.getWebScraperRequestListDto(5);
        List<WebScraperResponseDto> responseDto = MappersTestsHelper.getWebScraperResponseListDto(
                5);
        when(service.scrapSites(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    public void should_SitesEndpointReturn200_When_ListItemWithNullCookies() throws Exception {
        List<WebScraperRequestDto> requestDto = List.of(
                MappersTestsHelper.getWebScraperRequestDtoWithNullCookies());
        List<WebScraperResponseDto> responseDto = MappersTestsHelper.getWebScraperResponseListDto(
                1);

        when(service.scrapSites(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    public void should_SitesEndpointReturn200_When_ListItemWithEmptyCookies() throws Exception {
        List<WebScraperRequestDto> requestDto = List.of(
                MappersTestsHelper.getWebScraperRequestDtoWithEmptyCookies());
        List<WebScraperResponseDto> responseDto = MappersTestsHelper.getWebScraperResponseListDto(
                1);

        when(service.scrapSites(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    public void should_SitesEndpointReturn400_When_NoContent() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['Required request body is missing']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_EmptyList() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content("[]")
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests : must not be empty']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_ListItemWithNullId() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDtoWithNullId())))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests[0].id : must not be null']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_ListItemWithNullUrl() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDtoWithNullUrl())))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests[0].url : must not be blank']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_ListItemWithBlankUrl() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDtoWithBlankUrl())))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests[0].url : must not be blank','scrapSites.requests[0].url : must be a valid URL']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_ListItemWithNullCookieName() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieNullName())))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests[0].cookies[1].name : must not be blank']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_ListItemWithBlankCookieName() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankName())))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests[0].cookies[1].name : must not be blank']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_ListItemWithNullCookieValue() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieNullValue())))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests[0].cookies[1].value : must not be blank']}"));
    }

    @Test
    public void should_SitesEndpointReturn400_When_ListItemWithBlankCookieValue() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(
                                List.of(MappersTestsHelper.getWebScraperRequestDtoWithCookieBlankValue())))
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{'status':'BAD_REQUEST',"
                        + "'message':'Bad Request Data',"
                        + "'errors':['scrapSites.requests[0].cookies[1].value : must not be blank']}"));
    }
}
