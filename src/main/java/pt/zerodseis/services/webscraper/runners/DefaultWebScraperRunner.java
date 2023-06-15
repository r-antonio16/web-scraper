package pt.zerodseis.services.webscraper.runners;

import jakarta.annotation.PreDestroy;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pt.zerodseis.services.webscraper.connections.manager.WebScraperConnectionProviderManager;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;

@Log4j2
@Component
public class DefaultWebScraperRunner implements WebScraperRunner {

    private final ExecutorService executor;
    private final WebScraperConnectionProviderManager providersManager;
    private final long waitForScraperResponseTimeout;
    private final TimeUnit waitForScraperResponseUnit;

    public DefaultWebScraperRunner(WebScraperConnectionProviderManager providersManager,
            @Value("${runner.wait.for.scraper.response.timeout}") long waitForScraperResponseTimeout,
            @Value("${runner.wait.for.scraper.response.unit}") TimeUnit waitForScraperResponseUnit) {
        this.executor = Executors.newWorkStealingPool();
        this.providersManager = providersManager;
        this.waitForScraperResponseTimeout = waitForScraperResponseTimeout;
        this.waitForScraperResponseUnit = waitForScraperResponseUnit;
    }

    @Override
    public WebScraperResponse scrapSite(WebScraperRequest request) {

        Future<WebScraperResponse> future = executor.submit(
                new ScrapSiteCallable(providersManager, request));

        return waitForWebScraperResponse(request, future);
    }

    @Override
    public List<WebScraperResponse> scrapSites(List<WebScraperRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return new ArrayList<>();
        }

        List<SimpleImmutableEntry<WebScraperRequest, Future<WebScraperResponse>>> responseFutureList = new ArrayList<>();

        for (WebScraperRequest request : requests) {
            Future<WebScraperResponse> future = executor.submit(
                    new ScrapSiteCallable(providersManager, request));
            responseFutureList.add(new AbstractMap.SimpleImmutableEntry<>(request, future));
        }

        List<WebScraperResponse> responseList = new ArrayList<>();

        for (SimpleImmutableEntry<WebScraperRequest, Future<WebScraperResponse>> future : responseFutureList) {
            responseList.add(waitForWebScraperResponse(future.getKey(), future.getValue()));
        }

        return responseList;
    }

    private WebScraperResponse waitForWebScraperResponse(WebScraperRequest request,
            Future<WebScraperResponse> responseFuture) {
        try {
            return responseFuture.get(waitForScraperResponseTimeout, waitForScraperResponseUnit);
        } catch (TimeoutException e) {
            responseFuture.cancel(true);
            log.error("The task to get response for request {} took more time than expected",
                    request);
            return new WebScraperResponse(request, null, HttpStatus.REQUEST_TIMEOUT);
        } catch (InterruptedException e) {
            log.error("The task to get response for request {} was interrupted", request);
            return new WebScraperResponse(request, null, HttpStatus.REQUEST_TIMEOUT);
        } catch (ExecutionException e) {
            log.error("The task to get response for request {} could not complete with success",
                    request);
        }

        return new WebScraperResponse(request, null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PreDestroy
    protected void destroy() {
        executor.shutdownNow();
    }
}
