package pt.zerodseis.services.webscraper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.connections.HTTPConnection;
import pt.zerodseis.services.webscraper.connections.WebScraperConnectionProvider;
import pt.zerodseis.services.webscraper.connections.wrappers.URLConnectionWrapper;
import pt.zerodseis.services.webscraper.exceptions.SiteConnectionException;

@ExtendWith(SpringExtension.class)
public class IpAddressUtilTest {

    @Mock
    WebScraperConnectionProvider provider;

    @Test
    public void Should_ReturnExternalIp_When_Provider_Can_Open_New_Connection() throws IOException {
        String ip = "100.0.0.1";
        InputStream is = new ByteArrayInputStream(ip.getBytes());
        URLConnectionWrapper urlConnectionWrapper = mock(URLConnectionWrapper.class);
        HTTPConnection httpConnection = new HTTPConnection(UUID.randomUUID(), urlConnectionWrapper);

        when(urlConnectionWrapper.getResponseCode()).thenReturn(200);
        when(httpConnection.getInputStream()).thenReturn(is);
        when(provider.openConnection(any(URL.class))).thenReturn(Optional.of(httpConnection));

        InetAddress address = IpAddressUtil.getExternalIpAddress(provider);

        assertNotNull(address);
        assertEquals(ip, address.getHostAddress());
        verify(provider, times(1)).closeConnection(any(HTTPConnection.class));
    }

    @Test
    public void Should_ReturnNull_When_Http_Response_Is_Null() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[]{});
        URLConnectionWrapper urlConnectionWrapper = mock(URLConnectionWrapper.class);
        HTTPConnection httpConnection = spy(
                new HTTPConnection(UUID.randomUUID(), urlConnectionWrapper));

        when(urlConnectionWrapper.getResponseCode()).thenReturn(200);
        when(httpConnection.getInputStream()).thenReturn(is);
        when(provider.openConnection(any(URL.class))).thenReturn(Optional.of(httpConnection));

        InetAddress address = IpAddressUtil.getExternalIpAddress(provider);

        assertNull(address);
        verify(httpConnection, times(1)).getInputStream();
        verify(provider, times(1)).closeConnection(any(HTTPConnection.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {301, 404, 500})
    public void Should_ThrowException_When_Http_ResponseCode_Is_Not_Ok(int responseCode)
            throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[]{});
        URLConnectionWrapper urlConnectionWrapper = mock(URLConnectionWrapper.class);
        HTTPConnection httpConnection = spy(
                new HTTPConnection(UUID.randomUUID(), urlConnectionWrapper));

        when(urlConnectionWrapper.getResponseCode()).thenReturn(responseCode);
        when(httpConnection.getInputStream()).thenReturn(is);
        when(provider.openConnection(any(URL.class))).thenReturn(Optional.of(httpConnection));

        assertThrows(SiteConnectionException.class,
                () -> IpAddressUtil.getExternalIpAddress(provider));
    }

    @Test
    public void Should_ReturnNull_When_Provider_Cannot_Open_New_Connection() throws IOException {
        when(provider.openConnection(any(URL.class))).thenReturn(Optional.empty());

        InetAddress address = IpAddressUtil.getExternalIpAddress(provider);

        assertNull(address);
        verify(provider, never()).closeConnection(any(HTTPConnection.class));
    }

    @Test
    public void Should_ThrowGetExternalIpAddressException_When_IOException_Is_Thrown()
            throws IOException {
        URLConnectionWrapper urlConnectionWrapper = mock(URLConnectionWrapper.class);
        HTTPConnection httpConnection = spy(
                new HTTPConnection(UUID.randomUUID(), urlConnectionWrapper));

        when(urlConnectionWrapper.getResponseCode()).thenReturn(200);
        when(httpConnection.getInputStream()).thenThrow(IOException.class);
        when(provider.openConnection(any(URL.class))).thenReturn(Optional.of(httpConnection));

        assertThrows(SiteConnectionException.class,
                () -> IpAddressUtil.getExternalIpAddress(provider));

        verify(httpConnection, times(1)).getInputStream();
        verify(provider, times(1)).closeConnection(any(HTTPConnection.class));
    }
}
