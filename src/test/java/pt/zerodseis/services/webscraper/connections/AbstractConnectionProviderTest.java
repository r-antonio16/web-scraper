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
import java.net.HttpCookie;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;
import pt.zerodseis.services.webscraper.utils.UserAgentUtil;

@ExtendWith(SpringExtension.class)
public class AbstractConnectionProviderTest {

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_StatusBeUp_When_Is_Instantiated_Successfully(String clazz)
            throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_StatusBeDown_When_Does_Not_Have_Ip(String clazz) {
        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(null);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertNull(provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_Return_HTPPConnection_When_Status_Is_Up(String clazz) throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(connection);
        when(connection.getResponseCode()).thenReturn(200);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(9, provider.getFreeConnections());
            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "TorConnectionProvider,404", "DefaultConnectionProvider,404",
            "TorConnectionProvider,500", "DefaultConnectionProvider,500",
            "TorConnectionProvider,301", "DefaultConnectionProvider,301"
    })
    public void Should_Throw_Exception_When_ConnectionStatus_Is_Not_Ok(String clazz,
            int responseCode) throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(connection);
        when(connection.getResponseCode()).thenReturn(responseCode);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(9, provider.getFreeConnections());
            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(responseCode, optionalHTTPConnection.get().getResponseCode());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_Return_Empty_HTTPConnection_When_Status_Is_Down(String clazz)
            throws IOException {
        URL siteUrl = mock(URL.class);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(null);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertNull(provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(10, provider.getFreeConnections());
            assertTrue(optionalHTTPConnection.isEmpty());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_Close_HTPPConnection_When_closeConnection_Is_Called(String clazz)
            throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(connection);
        when(connection.getResponseCode()).thenReturn(200);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(9, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.closeConnection(optionalHTTPConnection.get());

            assertEquals(10, provider.getFreeConnections());
            verify(connection, times(1)).disconnect();
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }

    @ParameterizedTest
    @CsvSource({"100,100,TorConnectionProvider", "200,200,TorConnectionProvider",
            "500,500,TorConnectionProvider", "10,100,TorConnectionProvider",
            "20,200,TorConnectionProvider", "50,500,TorConnectionProvider",
            "100,100,DefaultConnectionProvider", "200,200,DefaultConnectionProvider",
            "500,500,DefaultConnectionProvider", "10,100,DefaultConnectionProvider",
            "20,200,DefaultConnectionProvider", "50,500,DefaultConnectionProvider"})
    public void Should_NotExceedMaxConnections_When_Concurrent_Threads_Opens_Connections(
            int maxConnections, int requestedSites, String clazz) throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        List<URL> sites = new ArrayList<>();
        LinkedBlockingQueue<Optional<HTTPConnection>> connections = new LinkedBlockingQueue<>();

        for (int i = 0; i < requestedSites; i++) {
            URL site = mock(URL.class);
            sites.add(site);
            HttpURLConnection connection = mock(HttpURLConnection.class);
            when(site.openConnection(any(Proxy.class))).thenReturn(connection);
            when(connection.getResponseCode()).thenReturn(200);
        }

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                    any(ConnectionsTestsHelper.getClassType(clazz)))).thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    maxConnections);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(maxConnections, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            try (ExecutorService es = Executors.newCachedThreadPool()) {
                for (int i = 0; i < requestedSites; i++) {
                    es.execute(
                            ConnectionsTestsHelper.getOpenConnectionRunnable(provider, sites.get(i),
                                    connections));
                }
                ConnectionsTestsHelper.awaitTerminationAfterShutdown(es);
            }

            assertEquals(0, provider.getFreeConnections());

            try (ExecutorService es = Executors.newCachedThreadPool()) {
                for (int i = 0; i < requestedSites; i++) {
                    Objects.requireNonNull(connections.poll()).ifPresent(c -> es.execute(
                            ConnectionsTestsHelper.getCloseConnectionRunnable(provider, c)));
                }
                ConnectionsTestsHelper.awaitTerminationAfterShutdown(es);
            }

            assertEquals(maxConnections, provider.getFreeConnections());
        }
    }

    @ParameterizedTest
    @CsvSource({"100,TorConnectionProvider", "200,TorConnectionProvider",
            "500,TorConnectionProvider", "100,DefaultConnectionProvider",
            "200,DefaultConnectionProvider", "500,DefaultConnectionProvider",})
    public void Should_closeAllConnections_When_isDestroyed(int activeConnections, String clazz)
            throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        List<URL> sites = new ArrayList<>();
        LinkedBlockingQueue<Optional<HTTPConnection>> connections = new LinkedBlockingQueue<>();

        for (int i = 0; i < activeConnections; i++) {
            URL site = mock(URL.class);
            sites.add(site);
            HttpURLConnection connection = mock(HttpURLConnection.class);
            when(site.openConnection(any(Proxy.class))).thenReturn(connection);
            when(connection.getResponseCode()).thenReturn(200);
        }

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                    any(ConnectionsTestsHelper.getClassType(clazz)))).thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    activeConnections);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(activeConnections, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            try (ExecutorService es = Executors.newCachedThreadPool()) {
                for (int i = 0; i < activeConnections; i++) {
                    es.execute(
                            ConnectionsTestsHelper.getOpenConnectionRunnable(provider, sites.get(i),
                                    connections));
                }
                ConnectionsTestsHelper.awaitTerminationAfterShutdown(es);
            }

            assertEquals(0, provider.getFreeConnections());

            ((AbstractConnectionProvider) provider).destroy();

            assertEquals(activeConnections, provider.getFreeConnections());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_scoreZero_When_isNotUp(String clazz) {
        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(null);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertNull(provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
            assertEquals(0, provider.score());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_scoreZero_When_NoAvailableConnections(String clazz)
            throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(connection);
        when(connection.getResponseCode()).thenReturn(200);

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    1);

            assertNotNull(provider);
            assertEquals(1, provider.getFreeConnections());
            assertEquals(ip, provider.getIp());

            provider.openConnection(siteUrl);

            assertEquals(0, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
            assertEquals(0, provider.score());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_scorePositive_When_AvailableConnections(String clazz)
            throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(
                            any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
            assertEquals(1000, provider.score());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"TorConnectionProvider", "DefaultConnectionProvider"})
    public void Should_HaveHTTPConnectionProps_When_UAAndCookiesArePassed(String clazz)
            throws IOException {
        InetAddress ip = InetAddress.getByName("100.0.0.1");
        URL siteUrl = mock(URL.class);
        HttpCookie cookie1 = new HttpCookie("session-id", "12345678");
        HttpCookie cookie2 = new HttpCookie("locale", "pt_PT");
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(siteUrl.openConnection(any(Proxy.class))).thenReturn(connection);
        when(connection.getResponseCode()).thenReturn(200);
        StringBuilder sb = new StringBuilder();
        String userAgent = UserAgentUtil.getRandomUserAgent();

        sb.append(String.format("%s=%s; ", cookie1.getName(), cookie1.getValue()));
        sb.append(String.format("%s=%s; ", cookie2.getName(), cookie2.getValue()));

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(
                                    any(ConnectionsTestsHelper.getClassType(clazz))))
                    .thenReturn(ip);

            WebScraperConnectionProvider provider = ConnectionsTestsHelper.getProviderInstance(
                    clazz,
                    10);

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl, null,
                    userAgent, cookie1, cookie2);

            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(9, provider.getFreeConnections());
            provider.closeConnection(optionalHTTPConnection.get());
            assertEquals(10, provider.getFreeConnections());
            verify(connection, times(1)).setRequestProperty("Cookie", sb.toString());
            verify(connection, times(1)).setRequestProperty("User-Agent", userAgent);
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());
        }
    }
}
