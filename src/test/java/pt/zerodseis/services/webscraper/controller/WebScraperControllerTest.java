package pt.zerodseis.services.webscraper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void should_SiteEndpointReturn400_When_NoContent() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/site")
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void should_SitesEndpointReturn200_When_ValidRequest() throws Exception {
        List<WebScraperRequestDto> requestDto = MappersTestsHelper.getWebScraperRequestListDto(5);
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void should_SitesEndpointReturn400_When_NoContent() throws Exception {
        mockMvc.perform(post("/api/v1/scraper/sites")
                        .contentType("application/json"))
                .andExpect(status().is4xxClientError());
    }
}
