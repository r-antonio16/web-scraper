package pt.zerodseis.services.webscraper.runners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.connections.DefaultConnectionProvider;
import pt.zerodseis.services.webscraper.connections.HTTPConnection;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.connections.manager.WebScraperConnectionProviderManager;
import pt.zerodseis.services.webscraper.exceptions.SiteConnectionException;

@ExtendWith(SpringExtension.class)
public class ScrapSiteCallableTest {

    @Test
    public void Should_NotOpenConnection_When_EmptyProvider() throws Exception {
        WebScraperConnectionProviderManager manager = mock(
                WebScraperConnectionProviderManager.class);
        URL url = mock(URL.class);
        WebScraperRequest request = new WebScraperRequest(1, url, null, null, null);
        ScrapSiteCallable callable = new ScrapSiteCallable(manager, request);

        when(manager.electProvider()).thenReturn(Optional.empty());

        WebScraperResponse response = callable.call();

        assertNotNull(response);
        assertNull(response.content());
        assertEquals(request, response.request());
        assertNull(response.statusCode());
        assertEquals(SiteScrapStatus.PROVIDER_UNAVAILABLE, response.siteScrapStatus());
        verify(url, times(0)).openConnection(any());
    }

    @Test
    public void Should_NotOpenConnection_When_EmptyConnection() throws Exception {
        DefaultConnectionProvider defaultConnectionProvider = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager manager = mock(
                WebScraperConnectionProviderManager.class);
        URL url = mock(URL.class);
        WebScraperRequest request = new WebScraperRequest(1, url, null, null, null);
        ScrapSiteCallable callable = new ScrapSiteCallable(manager, request);

        when(manager.electProvider()).thenReturn(Optional.of(defaultConnectionProvider));
        when(defaultConnectionProvider.score()).thenReturn(1);
        when(defaultConnectionProvider.openConnection(any(), any(), any())).thenReturn(
                Optional.empty());
        WebScraperResponse response = callable.call();

        assertNotNull(response);
        assertNull(response.content());
        assertEquals(request, response.request());
        assertNull(response.statusCode());
        assertEquals(SiteScrapStatus.CONNECTION_UNAVAILABLE, response.siteScrapStatus());
        verify(url, times(0)).openConnection(any());
    }

    @Test
    public void Should_UseRandomUA_When_NoneProvided() throws Exception {
        DefaultConnectionProvider defaultConnectionProvider = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager manager = mock(
                WebScraperConnectionProviderManager.class);
        URL url = mock(URL.class);
        WebScraperRequest request = new WebScraperRequest(1, url, null, null, null);
        ScrapSiteCallable callable = new ScrapSiteCallable(manager, request);

        ArgumentCaptor<String> uaCaptor = ArgumentCaptor.forClass(String.class);
        when(manager.electProvider()).thenReturn(Optional.of(defaultConnectionProvider));
        when(defaultConnectionProvider.score()).thenReturn(1);
        when(defaultConnectionProvider.openConnection(any(), any(), any())).thenReturn(
                Optional.empty());
        callable.call();

        verify(defaultConnectionProvider, times(1)).openConnection(any(), any(), uaCaptor.capture(),
                any());
        assertNotNull(uaCaptor.getValue());
    }

    @Test
    public void Should_ReturnOriginalHttpStatusCode_When_ConnectionResponseIsNotOk()
            throws Exception {
        DefaultConnectionProvider defaultConnectionProvider = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager manager = mock(
                WebScraperConnectionProviderManager.class);
        WebScraperRequest request = new WebScraperRequest(1, mock(URL.class), null, null, null);
        ScrapSiteCallable callable = new ScrapSiteCallable(manager, request);
        Optional<HTTPConnection> connection = RunnersTestsHelper.getNotFoundHTTPConnectionMock();

        when(manager.electProvider()).thenReturn(Optional.of(defaultConnectionProvider));
        when(defaultConnectionProvider.score()).thenReturn(1);
        when(defaultConnectionProvider.openConnection(any(), any(), any(), any())).thenReturn(
                connection);

        WebScraperResponse response = callable.call();

        assertNotNull(response);
        assertNull(response.content());
        assertEquals(request, response.request());
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
        assertEquals(SiteScrapStatus.SUCCESS, response.siteScrapStatus());
        verify(defaultConnectionProvider, times(1)).closeConnection(any());
    }

    @Test
    public void Should_ReturnOriginalHttpStatusCodeAndContent_When_ConnectionResponseIsOk()
            throws Exception {
        DefaultConnectionProvider defaultConnectionProvider = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager manager = mock(
                WebScraperConnectionProviderManager.class);
        WebScraperRequest request = new WebScraperRequest(1, mock(URL.class), null, null, null);
        ScrapSiteCallable callable = new ScrapSiteCallable(manager, request);
        Optional<HTTPConnection> connection = RunnersTestsHelper.getSuccessHTTPConnectionMock();

        when(manager.electProvider()).thenReturn(Optional.of(defaultConnectionProvider));
        when(defaultConnectionProvider.score()).thenReturn(1);
        when(defaultConnectionProvider.openConnection(any(), any(), any(), any())).thenReturn(
                connection);

        WebScraperResponse response = callable.call();

        assertNotNull(response);
        assertEquals("html content", response.content());
        assertEquals(request, response.request());
        assertEquals(HttpStatus.OK, response.statusCode());
        assertEquals(SiteScrapStatus.SUCCESS, response.siteScrapStatus());
        verify(defaultConnectionProvider, times(1)).closeConnection(any());
    }

    @Test
    public void Should_ThrowSiteConnectionException_When_AnExceptionIsThrownWhileReadingContent()
            throws Exception {
        DefaultConnectionProvider defaultConnectionProvider = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager manager = mock(
                WebScraperConnectionProviderManager.class);
        WebScraperRequest request = new WebScraperRequest(1, mock(URL.class), null, null, null);
        ScrapSiteCallable callable = new ScrapSiteCallable(manager, request);
        Optional<HTTPConnection> connection = RunnersTestsHelper.getSuccessHTTPConnectionAndBrokenInputStreamMock();

        when(manager.electProvider()).thenReturn(Optional.of(defaultConnectionProvider));
        when(defaultConnectionProvider.score()).thenReturn(1);
        when(defaultConnectionProvider.openConnection(any(), any(), any(), any())).thenReturn(
                connection);

        assertThrows(SiteConnectionException.class, callable::call);
        verify(defaultConnectionProvider, times(1)).closeConnection(any());
    }

    @Test
    public void Should_ThrowSiteConnectionException_When_AnExceptionIsThrownWhileOpeningConnection()
            throws IOException {
        DefaultConnectionProvider defaultConnectionProvider = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager manager = mock(
                WebScraperConnectionProviderManager.class);
        WebScraperRequest request = new WebScraperRequest(1, mock(URL.class), null, null, null);
        ScrapSiteCallable callable = new ScrapSiteCallable(manager, request);

        when(manager.electProvider()).thenReturn(Optional.of(defaultConnectionProvider));
        when(defaultConnectionProvider.score()).thenReturn(1);
        when(defaultConnectionProvider.openConnection(any(), any(), any(), any())).thenThrow(
                IOException.class);

        assertThrows(SiteConnectionException.class, callable::call);
    }
}
