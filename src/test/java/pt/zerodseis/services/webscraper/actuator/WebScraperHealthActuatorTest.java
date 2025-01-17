package pt.zerodseis.services.webscraper.actuator;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WebScraperHealthActuatorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TorConnectionProvider torConnectionProvider;

    @MockBean
    private DefaultConnectionProvider defaultConnectionProvider;

    @Test
    public void should_HealthBeUp_When_AnyWebConnectionProviderIsUp() throws Exception {
        when(torConnectionProvider.score()).thenReturn(10);
        when(defaultConnectionProvider.score()).thenReturn(10);

        mockMvc.perform(get("/actuator/health").contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'status': 'UP'}"));
    }

    @Test
    public void should_HealthBeDown_When_NoWebConnectionProviderIsUp() throws Exception {
        when(torConnectionProvider.score()).thenReturn(0);
        when(defaultConnectionProvider.score()).thenReturn(0);

        mockMvc.perform(get("/actuator/health").contentType("application/json"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().json("{'status': 'DOWN'}"));
    }
}
