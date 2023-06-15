package pt.zerodseis.services.webscraper.connections.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.connections.DefaultConnectionProvider;
import pt.zerodseis.services.webscraper.connections.TorConnectionProvider;
import pt.zerodseis.services.webscraper.connections.WebScraperConnectionProvider;

@ExtendWith(SpringExtension.class)
public class WebScraperConnectionProviderManagerTest {

    @Test
    public void Should_ReturnDefaultProvider_When_OtherProvidersDoesNotHaveScore() {
        TorConnectionProvider provider1 = mock(TorConnectionProvider.class);
        TorConnectionProvider provider2 = mock(TorConnectionProvider.class);
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        when(provider1.score()).thenReturn(1);
        when(provider2.score()).thenReturn(1);
        when(defaultProviderMock.score()).thenReturn(1);

        WebScraperConnectionProviderManager providersManager = new WebScraperConnectionProviderManager(
                List.of(provider1, provider2), defaultProviderMock);

        Optional<WebScraperConnectionProvider> provider = providersManager.electProvider();

        assertTrue(provider.isPresent());
        assertEquals(defaultProviderMock, provider.get());
    }

    @Test
    public void Should_ReturnNonDefaultProvider_When_AnyProviderHasScore() {
        TorConnectionProvider winnerProviderMock = mock(TorConnectionProvider.class);
        TorConnectionProvider loserProviderMock = mock(TorConnectionProvider.class);
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        when(winnerProviderMock.score()).thenReturn(10);
        when(loserProviderMock.score()).thenReturn(5);
        when(defaultProviderMock.score()).thenReturn(15);

        WebScraperConnectionProviderManager providersManager = new WebScraperConnectionProviderManager(
                List.of(winnerProviderMock, loserProviderMock), defaultProviderMock);

        Optional<WebScraperConnectionProvider> provider = providersManager.electProvider();

        assertTrue(provider.isPresent());
        assertEquals(winnerProviderMock, provider.get());
    }

    @Test
    public void Should_ReturnEmpty_When_NoProviderHasScore() {
        TorConnectionProvider provider1 = mock(TorConnectionProvider.class);
        TorConnectionProvider provider2 = mock(TorConnectionProvider.class);
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        when(provider1.score()).thenReturn(1);
        when(provider2.score()).thenReturn(1);
        when(defaultProviderMock.score()).thenReturn(0);

        WebScraperConnectionProviderManager providersManager = new WebScraperConnectionProviderManager(
                List.of(provider1, provider2), defaultProviderMock);

        Optional<WebScraperConnectionProvider> provider = providersManager.electProvider();

        assertTrue(provider.isEmpty());
    }
}
