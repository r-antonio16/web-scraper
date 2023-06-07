package pt.zerodseis.services.webscraper.connections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.exceptions.RenewExternalIpAddressException;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;

@ExtendWith(SpringExtension.class)
public class TorConnectionProviderTest {

    @Test
    public void Should_StatusBeUp_When_Is_Instantiated_Successfully()
            throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @Test
    public void Should_StatusBeDown_When_Does_Not_Have_Ip() {
        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(null);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

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
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

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
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(null);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

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
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

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

    @Test
    public void Should_NotRenewIP_When_There_Are_ActiveConnections() throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(connection);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(1, provider.getActiveConnections());
            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.renewIp();

            assertEquals(1, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @Test
    public void Should_RenewIP_When_There_Are_No_ActiveConnections() throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    getRestartPathScriptMockByOS(),
                    5,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.renewIp();

            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @Test
    public void Should_StatusBeDown_When_Could_Not_Run_RestartScript() throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    getRestartPathScriptSleepMockByOS(),
                    1,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.renewIp();

            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
        }
    }

    @Test
    public void Should_ThrowRenewExternalIpAddressException_When_AnyException() throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(mock(HttpURLConnection.class));

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    getRestartPathScriptMockByOS(),
                    5,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenThrow(RuntimeException.class);

            assertThrows(RenewExternalIpAddressException.class, provider::renewIp);

            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 200, 500})
    public void Should_OpenAndCloseMultipleConnections_When_Concurrent_Threads_Run_It(
            int requestedSites)
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
                            () -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = new TorConnectionProvider(
                    "127.0.0.1",
                    5000,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(0, provider.getActiveConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            try (ExecutorService es = Executors.newCachedThreadPool()) {
                for (int i = 0; i < requestedSites; i++) {
                    es.execute(getOpenConnectionRunnable(provider, sites.get(i), connections));
                }
                awaitTerminationAfterShutdown(es);
            }

            assertEquals(requestedSites, provider.getActiveConnections());

            try (ExecutorService es = Executors.newCachedThreadPool()) {
                for (int i = 0; i < requestedSites; i++) {
                    Objects.requireNonNull(connections.poll())
                            .ifPresent(c ->
                                    es.execute(getCloseConnectionRunnable(provider, c))
                            );
                }
                awaitTerminationAfterShutdown(es);
            }

            assertEquals(0, provider.getActiveConnections());
        }
    }

    private Runnable getOpenConnectionRunnable(WebScraperConnectionProvider provider, URL url,
            LinkedBlockingQueue<Optional<HTTPConnection>> connections) {
        return () -> {
            try {
                connections.add(provider.openConnection(url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable getCloseConnectionRunnable(WebScraperConnectionProvider provider,
            HTTPConnection connection) {
        return () -> provider.closeConnection(connection);
    }

    private String getRestartPathScriptMockByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "/c exit(0)" : "-c exit(0)";
    }

    private String getRestartPathScriptSleepMockByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "/c timeout /t 5" : "-c sleep 5";
    }

    private void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
