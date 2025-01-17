package pt.zerodseis.services.webscraper.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pt.zerodseis.services.webscraper.connections.DefaultConnectionProvider;
import pt.zerodseis.services.webscraper.connections.TorConnectionProvider;
import pt.zerodseis.services.webscraper.mapper.MappersTestsHelper;
import pt.zerodseis.services.webscraper.service.WebScraperService;
import pt.zerodseis.services.webscraper.dto.WebScraperRequestDto;
import pt.zerodseis.services.webscraper.dto.WebScraperResponseDto;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JacksonConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebScraperService service;

    @MockBean
    private TorConnectionProvider torConnectionProvider;

    @MockBean
    private DefaultConnectionProvider defaultConnectionProvider;

    @Test
    public void should_NotSerializeNulls_When_JacksonConfigIsEnabled() throws Exception {
        WebScraperRequestDto requestDto = MappersTestsHelper.getWebScraperRequestDto();
        WebScraperResponseDto responseDto = new WebScraperResponseDto(
                1,
                null,
                null,
                null
        );
        when(service.scrapSite(requestDto)).thenReturn(responseDto);
        mockMvc.perform(post("/api/v1/scraper/site")
                        .content(objectMapper.writeValueAsBytes(requestDto))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'requestId':1}"));
    }

}
