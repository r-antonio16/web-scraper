package pt.zerodseis.services.webscraper.connections.wrappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.exceptions.SiteConnectionInitException;
import pt.zerodseis.services.webscraper.utils.UserAgentUtil;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class DefaultURLConnectionWrapperTest {

    private URL urlMock;
    private HttpURLConnection connectionMock;
    private static final Proxy proxyMock = Proxy.NO_PROXY;

    @BeforeEach
    public void setupMocks() throws IOException {
        urlMock = mock(URL.class);
        connectionMock = mock(HttpURLConnection.class);
        when(urlMock.openConnection(proxyMock)).thenReturn(connectionMock);
    }

    @Test
    public void Should_Instantiate_When_args_are_valid() {
        HttpCookie cookie1 = new HttpCookie("session-id", "12345678");
        HttpCookie cookie2 = new HttpCookie("locale", "pt_PT");
        StringBuilder sb = new StringBuilder();
        String userAgent = UserAgentUtil.getRandomUserAgent();

        sb.append(String.format("%s=%s; ", cookie1.getName(), cookie1.getValue()));
        sb.append(String.format("%s=%s; ", cookie2.getName(), cookie2.getValue()));

        Map<String, List<Object>> requestProperties = Map.of("User-Agent", List.of(userAgent),
                "Cookie", List.of(cookie1, cookie2));

        DefaultURLConnectionWrapper connectionWrapper = new DefaultURLConnectionWrapper(urlMock, proxyMock,
                requestProperties);

        assertNotNull(connectionWrapper);
        verify(connectionMock, times(1)).setRequestProperty("Cookie", sb.toString());
        verify(connectionMock, times(1)).setRequestProperty("User-Agent", userAgent);
    }

    @ParameterizedTest
    @MethodSource("getArgsToTest")
    public void Should_Throw_SiteConnectionInitException_When_args_are_invalid(URL url, Proxy proxy, Map<String,
            List<Object>> requestProperties) {

        assertThrows(SiteConnectionInitException.class, () -> new DefaultURLConnectionWrapper(url, proxy,
                requestProperties));
    }

    @Test
    public void Should_invoke_connection_connect_When_connect_is_invoked() throws IOException {
        DefaultURLConnectionWrapper connectionWrapper = new DefaultURLConnectionWrapper(urlMock, proxyMock,
                Collections.emptyMap());

        connectionWrapper.connect();

        verify(connectionMock, times(1)).connect();
    }

    @Test
    public void Should_invoke_connection_disconnect_When_disconnect_is_invoked() {
        DefaultURLConnectionWrapper connectionWrapper = new DefaultURLConnectionWrapper(urlMock, proxyMock,
                Collections.emptyMap());

        connectionWrapper.disconnect();

        verify(connectionMock, times(1)).disconnect();
    }

    @Test
    public void Should_invoke_connection_getResponseCode_When_getResponseCode_is_invoked() throws IOException {
        DefaultURLConnectionWrapper connectionWrapper = new DefaultURLConnectionWrapper(urlMock, proxyMock,
                Collections.emptyMap());

        connectionWrapper.getResponseCode();

        verify(connectionMock, times(1)).getResponseCode();
    }

    @Test
    public void Should_invoke_connection_getInputStream_When_getInputStream_is_invoked() throws IOException {
        DefaultURLConnectionWrapper connectionWrapper = new DefaultURLConnectionWrapper(urlMock, proxyMock,
                Collections.emptyMap());

        connectionWrapper.getInputStream();

        verify(connectionMock, times(1)).getInputStream();
    }

    static Stream<Arguments> getArgsToTest() {
        return Stream.of(
                Arguments.of(null,
                        proxyMock, Collections.emptyMap()),
                Arguments.of(mock(URL.class),
                        proxyMock, null),
                Arguments.of(null,
                        null, null)
        );
    }
}
