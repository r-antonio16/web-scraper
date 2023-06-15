package pt.zerodseis.services.webscraper.runners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.connections.DefaultConnectionProvider;
import pt.zerodseis.services.webscraper.connections.HTTPConnection;
import pt.zerodseis.services.webscraper.connections.TorConnectionProvider;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.connections.manager.WebScraperConnectionProviderManager;

@ExtendWith(SpringExtension.class)
public class DefaultWebScraperRunnerTest {

    @Test
    public void Should_BeInstantiatedSuccessfully_When_HasProviders() {
        TorConnectionProvider torProviderMock = mock(TorConnectionProvider.class);
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                List.of(torProviderMock), defaultProviderMock);
        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 1,
                TimeUnit.SECONDS);

        assertNotNull(runner);
    }

    @Test
    public void Should_CloseExecutorService_When_DestroyMethodIsCalled() {
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                Collections.emptyList(), defaultProviderMock);

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 1,
                TimeUnit.SECONDS);

        runner.destroy();
        assertNotNull(runner);
        assertThrows(RejectedExecutionException.class,
                () -> runner.scrapSite(new WebScraperRequest(null, null, null)));
        assertThrows(RejectedExecutionException.class,
                () -> runner.scrapSites(List.of(new WebScraperRequest(null, null, null))));
    }

    @Test
    public void Should_AbortScrapSite_When_TimeoutIsReached() throws IOException {
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        WebScraperRequest request = RunnersTestsHelper.generateWebScraperRequests(1).get(0);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                Collections.emptyList(), defaultProviderMock);
        when(defaultProviderMock.score()).thenReturn(10);
        when(defaultProviderMock.openConnection(any(), any(), any())).thenAnswer(i -> {
            Thread.sleep(5000);
            return Optional.empty();
        });

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 1,
                TimeUnit.SECONDS);

        WebScraperResponse response = runner.scrapSite(request);
        assertNotNull(response);
        assertNull(response.content());
        assertEquals(request, response.request());
        assertNull(response.statusCode());
        assertEquals(ScrapTaskStatus.REQUEST_TIMEOUT, response.scrapTaskStatus());
    }

    @Test
    public void Should_AbortScrapSite_When_ExceptionIsThrownInCallable() throws IOException {
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        WebScraperRequest request = RunnersTestsHelper.generateWebScraperRequests(1).get(0);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                Collections.emptyList(), defaultProviderMock);
        when(defaultProviderMock.score()).thenReturn(10);
        when(defaultProviderMock.openConnection(any(), any(), any())).thenThrow(IOException.class);

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 1,
                TimeUnit.SECONDS);

        WebScraperResponse response = runner.scrapSite(request);

        assertNotNull(response);
        assertNull(response.content());
        assertEquals(request, response.request());
        assertNull(response.statusCode());
        assertEquals(ScrapTaskStatus.REQUEST_ERROR, response.scrapTaskStatus());
    }

    @Test
    public void Should_ElectProviderWithHigherScore_When_OnSiteScrapRequest() throws IOException {
        TorConnectionProvider winnerProviderMock = mock(TorConnectionProvider.class);
        TorConnectionProvider loserProviderMock = mock(TorConnectionProvider.class);
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                List.of(winnerProviderMock, loserProviderMock), defaultProviderMock);
        WebScraperRequest request = RunnersTestsHelper.generateWebScraperRequests(1).get(0);
        Optional<HTTPConnection> connectionOptMock = RunnersTestsHelper.getSuccessHTTPConnectionMock();
        when(winnerProviderMock.score()).thenReturn(10);
        when(loserProviderMock.score()).thenReturn(5);
        when(loserProviderMock.score()).thenReturn(1);
        when(winnerProviderMock.openConnection(any(), any(), any())).thenReturn(connectionOptMock);
        when(loserProviderMock.openConnection(any(), any(), any())).thenReturn(Optional.empty());
        when(defaultProviderMock.openConnection(any(), any(), any())).thenReturn(Optional.empty());

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 1,
                TimeUnit.SECONDS);

        WebScraperResponse response = runner.scrapSite(request);

        assertNotNull(response);
        verify(winnerProviderMock, times(1)).openConnection(any(), any(), any());
        assertEquals("html content", response.content());
        assertEquals(request, response.request());
        assertEquals(HttpStatus.OK, response.statusCode());
    }

    @Test
    public void Should_ReturnScrapedSiteContent_When_ProviderUrlConnectionReturnsSuccessResponse()
            throws IOException {
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        WebScraperRequest request = RunnersTestsHelper.generateWebScraperRequests(1).get(0);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                Collections.emptyList(), defaultProviderMock);
        Optional<HTTPConnection> connectionOptMock = RunnersTestsHelper.getSuccessHTTPConnectionMock();
        when(defaultProviderMock.score()).thenReturn(10);
        when(defaultProviderMock.openConnection(any(), any(), any())).thenReturn(connectionOptMock);

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 1,
                TimeUnit.SECONDS);

        WebScraperResponse response = runner.scrapSite(request);

        assertNotNull(response);
        assertEquals("html content", response.content());
        assertEquals(request, response.request());
        assertEquals(HttpStatus.OK, response.statusCode());
    }

    @Test
    public void Should_AbortScrapSites_When_TimeoutIsReached() throws IOException {
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        List<WebScraperRequest> requests = RunnersTestsHelper.generateWebScraperRequests(10);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                Collections.emptyList(), defaultProviderMock);
        when(defaultProviderMock.score()).thenReturn(10);
        when(defaultProviderMock.openConnection(any(), any(), any())).thenAnswer(i -> {
            Thread.sleep(3000);
            return Optional.empty();
        });

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 50,
                TimeUnit.MILLISECONDS);

        List<WebScraperResponse> responses = runner.scrapSites(requests);

        assertNotNull(responses);
        assertEquals(requests.size(), responses.size());

        for (WebScraperResponse response : responses) {
            assertNull(response.content());
            assertNotNull(response.request());
            assertNull(response.statusCode());
            assertTrue(requests.contains(response.request()));
            assertEquals(ScrapTaskStatus.REQUEST_TIMEOUT, response.scrapTaskStatus());
        }
    }

    @Test
    public void Should_AbortOnlyScrapedSites_When_ExceptionIsThrown() throws IOException {
        DefaultConnectionProvider defaultProviderMock = mock(DefaultConnectionProvider.class);
        List<WebScraperRequest> requests = RunnersTestsHelper.generateWebScraperRequests(10);
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                Collections.emptyList(), defaultProviderMock);
        AtomicInteger openConnectionCalls = new AtomicInteger();
        Optional<HTTPConnection> connectionOptMock = RunnersTestsHelper.getSuccessHTTPConnectionMock();
        when(defaultProviderMock.score()).thenReturn(10);

        when(defaultProviderMock.openConnection(any(), any(), any())).thenAnswer(i -> {
            if (openConnectionCalls.incrementAndGet() % 2 == 0) {
                return connectionOptMock;
            } else {
                throw new IOException();
            }
        });

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 50,
                TimeUnit.MILLISECONDS);

        List<WebScraperResponse> responses = runner.scrapSites(requests);

        assertNotNull(responses);
        assertEquals(requests.size(), responses.size());

        for (WebScraperResponse response : responses) {
            assertNotNull(response.request());
            assertTrue(requests.contains(response.request()));
            assertTrue(ScrapTaskStatus.REQUEST_ERROR.equals(response.scrapTaskStatus())
                    || ScrapTaskStatus.REQUEST_SUCCESS.equals(response.scrapTaskStatus()));
        }
    }

    @ParameterizedTest
    @CsvSource({"15,10,100", "10,20,100", "15,10,0", "10,20,0", "7,25,100", "6,25,0", "10,50,150",
            "8,30,100"})
    public void Should_dispatchAllRequestsWithSuccess_When_MultipleSitesRequests(
            int maxActiveConnections, int requestsToGen, int connectionWaitMillis) {
        TorConnectionProvider torProvider1 = spy(
                RunnersTestsHelper.getTorConnectionProvider(maxActiveConnections / 2));
        TorConnectionProvider torProvider2 = spy(
                RunnersTestsHelper.getTorConnectionProvider(maxActiveConnections / 4));
        DefaultConnectionProvider defaultProvider = spy(
                RunnersTestsHelper.getDefaultConnectionProvider(maxActiveConnections));
        WebScraperConnectionProviderManager providerManager = new WebScraperConnectionProviderManager(
                List.of(torProvider1, torProvider2), defaultProvider);

        List<WebScraperRequest> requests1 = RunnersTestsHelper.generateWebScraperRequests(
                requestsToGen, connectionWaitMillis,
                () -> RunnersTestsHelper.getSuccessHTTPConnectionMock().get().connection());
        List<WebScraperRequest> requests2 = RunnersTestsHelper.generateWebScraperRequests(
                requestsToGen, connectionWaitMillis,
                () -> RunnersTestsHelper.getSuccessHTTPConnectionMock().get().connection());
        List<WebScraperRequest> requests3 = RunnersTestsHelper.generateWebScraperRequests(
                requestsToGen, connectionWaitMillis,
                () -> RunnersTestsHelper.getSuccessHTTPConnectionMock().get().connection());

        DefaultWebScraperRunner runner = new DefaultWebScraperRunner(providerManager, 1,
                TimeUnit.SECONDS);

        List<WebScraperResponse> responses1 = runner.scrapSites(requests1);
        List<WebScraperResponse> responses2 = runner.scrapSites(requests2);
        List<WebScraperResponse> responses3 = runner.scrapSites(requests3);

        assertNotNull(responses1);
        assertEquals(requests1.size(), responses1.size());

        int failedRequests = 0;

        for (WebScraperResponse response : responses1) {
            assertNotNull(response.request());

            if (response.content() == null) {
                failedRequests++;
                assertTrue(requests1.contains(response.request()));
                assertNull(response.statusCode());
                assertEquals(ScrapTaskStatus.CONNECTION_UNAVAILABLE, response.scrapTaskStatus());
            } else {
                assertEquals("html content", response.content());
                assertTrue(requests1.contains(response.request()));
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(ScrapTaskStatus.REQUEST_SUCCESS, response.scrapTaskStatus());
            }
        }

        assertNotNull(responses2);
        assertEquals(requests1.size(), responses2.size());

        for (WebScraperResponse response : responses2) {
            assertNotNull(response.request());

            if (response.content() == null) {
                failedRequests++;
                assertTrue(requests2.contains(response.request()));
                assertNull(response.statusCode());
                assertEquals(ScrapTaskStatus.CONNECTION_UNAVAILABLE, response.scrapTaskStatus());
            } else {
                assertEquals("html content", response.content());
                assertTrue(requests2.contains(response.request()));
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(ScrapTaskStatus.REQUEST_SUCCESS, response.scrapTaskStatus());
            }
        }

        assertNotNull(responses3);
        assertEquals(requests1.size(), responses3.size());

        for (WebScraperResponse response : responses3) {
            assertNotNull(response.request());

            if (response.content() == null) {
                failedRequests++;
                assertTrue(requests3.contains(response.request()));
                assertNull(response.statusCode());
                assertEquals(ScrapTaskStatus.CONNECTION_UNAVAILABLE, response.scrapTaskStatus());
            } else {
                assertEquals("html content", response.content());
                assertTrue(requests3.contains(response.request()));
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(ScrapTaskStatus.REQUEST_SUCCESS, response.scrapTaskStatus());
            }
        }

        if (failedRequests > 0) {
            System.out.printf(
                    "Should_dispatchAllRequestsWithSuccess_When_MultipleSitesRequests [%d,%d,%d] Failed requests %d%n",
                    maxActiveConnections, requestsToGen, connectionWaitMillis, failedRequests);
        }

        // Accept only 5% of request failures
        assertTrue(failedRequests <= 3 * requestsToGen * 0.00);
    }
}
