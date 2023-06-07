package pt.zerodseis.services.webscraper.connections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;

@ExtendWith(SpringExtension.class)
public class DefaultConnectionProviderTest {

    @Test
    public void Should_StatusBeUp_When_Is_Instantiated_Successfully()
            throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(any(DefaultConnectionProvider.class)))
                    .thenReturn(ip);

            DefaultConnectionProvider provider = new DefaultConnectionProvider(10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @Test
    public void Should_StatusBeDown_When_Does_Not_Have_Ip() {
        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(any(DefaultConnectionProvider.class)))
                    .thenReturn(null);

            DefaultConnectionProvider provider = new DefaultConnectionProvider(10);

            assertNotNull(provider);
            assertNull(provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
        }
    }

    @Test
    public void Should_Return_HTPPConnection_When_Status_Is_Up() throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(mock(HttpURLConnection.class));

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(any(DefaultConnectionProvider.class)))
                    .thenReturn(ip);

            DefaultConnectionProvider provider = new DefaultConnectionProvider(10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(1, provider.getActiveConnections());
            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @Test
    public void Should_Return_Empty_HTTPConnection_When_Status_Is_Down() throws IOException {
        URL siteUrl = mock(URL.class);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(any(DefaultConnectionProvider.class)))
                    .thenReturn(null);

            DefaultConnectionProvider provider = new DefaultConnectionProvider(10);

            assertNotNull(provider);
            assertNull(provider.getIp());
            assertEquals(0, provider.getActiveConnections());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(0, provider.getActiveConnections());
            assertTrue(optionalHTTPConnection.isEmpty());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
        }
    }

    @Test
    public void Should_Close_HTPPConnection_When_closeConnection_Is_Called() throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(connection);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(any(DefaultConnectionProvider.class)))
                    .thenReturn(ip);

            DefaultConnectionProvider provider = new DefaultConnectionProvider(10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(1, provider.getActiveConnections());
            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.closeConnection(optionalHTTPConnection.get());

            assertEquals(0, provider.getActiveConnections());
            verify(connection, times(1)).disconnect();
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @ParameterizedTest
    @CsvSource({"100,100", "200,200", "500,500", "10,100", "20, 200", "50, 500"})
    public void Should_NotExceedMaxConnections_When_Concurrent_Threads_Opens_Connections(
            int maxConnections, int requestedSites)
            throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        List<URL> sites = new ArrayList<>();
        LinkedBlockingQueue<Optional<HTTPConnection>> connections = new LinkedBlockingQueue<>();

        for (int i = 0; i < requestedSites; i++) {
            URL site = mock(URL.class);
            sites.add(site);
            when(site.openConnection(any(Proxy.class))).thenReturn(
                    mock(HttpURLConnection.class));
        }

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(any(DefaultConnectionProvider.class)))
                    .thenReturn(ip);

            DefaultConnectionProvider provider = new DefaultConnectionProvider(maxConnections);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            try (ExecutorService es = Executors.newCachedThreadPool()) {
                for (int i = 0; i < requestedSites; i++) {
                    es.execute(ProviderTestHelper.getOpenConnectionRunnable(provider, sites.get(i),
                            connections));
                }
                ProviderTestHelper.awaitTerminationAfterShutdown(es);
            }

            assertEquals(maxConnections, provider.getActiveConnections());

            try (ExecutorService es = Executors.newCachedThreadPool()) {
                for (int i = 0; i < requestedSites; i++) {
                    Objects.requireNonNull(connections.poll())
                            .ifPresent(c ->
                                    es.execute(
                                            ProviderTestHelper.getCloseConnectionRunnable(provider,
                                                    c))
                            );
                }
                ProviderTestHelper.awaitTerminationAfterShutdown(es);
            }

            assertEquals(0, provider.getActiveConnections());
        }
    }


}
