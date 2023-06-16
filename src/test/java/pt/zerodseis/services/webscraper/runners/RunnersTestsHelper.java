package pt.zerodseis.services.webscraper.runners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mockito.MockedStatic;
import pt.zerodseis.services.webscraper.connections.DefaultConnectionProvider;
import pt.zerodseis.services.webscraper.connections.HTTPConnection;
import pt.zerodseis.services.webscraper.connections.TorConnectionProvider;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;
import pt.zerodseis.services.webscraper.utils.UserAgentUtil;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RunnersTestsHelper {

    static List<WebScraperRequest> generateWebScraperRequests(int requestsToGen) {
        return generateWebScraperRequests(requestsToGen, 0, null);
    }

    static List<WebScraperRequest> generateWebScraperRequests(int requestsToGen,
            long connectionWaitMillis, Supplier<HttpURLConnection> mockConnectionConsumer) {
        List<WebScraperRequest> requests = new ArrayList<>();

        for (int i = 0; i < requestsToGen; i++) {
            HttpCookie sessionId = new HttpCookie("sessionId", UUID.randomUUID().toString());
            URL siteMock = mock(URL.class);
            if (mockConnectionConsumer != null) {
                HttpURLConnection mockConnection = mockConnectionConsumer.get();
                try {
                    when(siteMock.openConnection(any())).thenAnswer(inv -> {
                        Thread.sleep(connectionWaitMillis);
                        return mockConnection;
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            requests.add(new WebScraperRequest(i, siteMock, new HttpCookie[]{sessionId},
                    UserAgentUtil.getRandomUserAgent()));
        }

        return requests;
    }

    static TorConnectionProvider getTorConnectionProvider(int maxActiveConnections) {
        InetAddress ip = null;

        try {
            ip = InetAddress.getByName("101.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(() -> IpAddressUtil.getExternalIpAddress(any(TorConnectionProvider.class)))
                    .thenReturn(ip);

            return new TorConnectionProvider("127.0.0.1", 5000, maxActiveConnections, "", 1,
                    TimeUnit.SECONDS);
        }
    }

    static DefaultConnectionProvider getDefaultConnectionProvider(int maxActiveConnections) {
        InetAddress ip = null;

        try {
            ip = InetAddress.getByName("102.0.0.2");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        try (MockedStatic<IpAddressUtil> util = mockStatic(IpAddressUtil.class)) {
            util.when(
                            () -> IpAddressUtil.getExternalIpAddress(any(DefaultConnectionProvider.class)))
                    .thenReturn(ip);

            return new DefaultConnectionProvider(maxActiveConnections);
        }
    }

    static Optional<HTTPConnection> getNotFoundHTTPConnectionMock() {
        try {
            HttpURLConnection httpURLConnectionMock = mock(HttpURLConnection.class);
            when(httpURLConnectionMock.getResponseCode()).thenReturn(404);
            HTTPConnection connection = new HTTPConnection(UUID.randomUUID(),
                    httpURLConnectionMock);
            return Optional.of(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Optional<HTTPConnection> getSuccessHTTPConnectionMock() {
        try {
            HttpURLConnection httpURLConnectionMock = mock(HttpURLConnection.class);
            when(httpURLConnectionMock.getResponseCode()).thenReturn(200);
            InputStream inputStream = new ByteArrayInputStream("html content".getBytes());
            when(httpURLConnectionMock.getInputStream()).thenReturn(inputStream);
            HTTPConnection connection = new HTTPConnection(UUID.randomUUID(),
                    httpURLConnectionMock);
            return Optional.of(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Optional<HTTPConnection> getSuccessHTTPConnectionAndBrokenInputStreamMock() {
        try {
            HttpURLConnection httpURLConnectionMock = mock(HttpURLConnection.class);
            when(httpURLConnectionMock.getResponseCode()).thenReturn(200);
            InputStream inputStream = mock(InputStream.class);
            when(inputStream.read()).thenThrow(IOException.class);
            when(httpURLConnectionMock.getInputStream()).thenReturn(inputStream);
            HTTPConnection connection = new HTTPConnection(UUID.randomUUID(),
                    httpURLConnectionMock);
            return Optional.of(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
