package pt.zerodseis.services.webscraper.connections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.exceptions.RenewExternalIpAddressException;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;

@ExtendWith(SpringExtension.class)
public class TorConnectionProviderTest {

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
                    10,
                    "",
                    1,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());

            Optional<HTTPConnection> optionalHTTPConnection = provider.openConnection(siteUrl);

            assertEquals(9, provider.getFreeConnections());
            assertTrue(optionalHTTPConnection.isPresent());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.renewIp();

            assertEquals(9, provider.getFreeConnections());
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
                    10,
                    getRestartPathScriptMockByOS(),
                    5,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.renewIp();

            assertEquals(10, provider.getFreeConnections());
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
                    10,
                    getRestartPathScriptSleepMockByOS(),
                    1,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            provider.renewIp();

            assertEquals(10, provider.getFreeConnections());
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
                    10,
                    getRestartPathScriptMockByOS(),
                    5,
                    TimeUnit.SECONDS
            );

            assertNotNull(provider);
            assertEquals(ip, provider.getIp());
            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.UP, provider.getStatus());

            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenThrow(RuntimeException.class);

            assertThrows(RenewExternalIpAddressException.class, provider::renewIp);

            assertEquals(10, provider.getFreeConnections());
            assertEquals(WebScraperConnectionProviderStatus.DOWN, provider.getStatus());
        }
    }

    private String getRestartPathScriptMockByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "/c exit(0)" : "-c exit(0)";
    }

    private String getRestartPathScriptSleepMockByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "/c timeout /t 5" : "-c sleep 5";
    }
}
