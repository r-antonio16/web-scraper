package pt.zerodseis.services.webscraper.runners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import pt.zerodseis.services.webscraper.connections.HTTPConnection;
import pt.zerodseis.services.webscraper.connections.WebScraperConnectionProvider;
import pt.zerodseis.services.webscraper.connections.manager.WebScraperConnectionProviderManager;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.exceptions.SiteConnectionException;
import pt.zerodseis.services.webscraper.utils.UserAgentUtil;

@AllArgsConstructor
class ScrapSiteCallable implements Callable<WebScraperResponse> {

    private final WebScraperConnectionProviderManager providerManager;
    private final WebScraperRequest request;

    @Override
    public WebScraperResponse call() throws Exception {
        Optional<HTTPConnection> connectionOpt = Optional.empty();
        Optional<WebScraperConnectionProvider> providerOpt = providerManager.electProvider();

        if (providerOpt.isEmpty()) {
            return new WebScraperResponse(request, null, null,
                    SiteScrapStatus.PROVIDER_UNAVAILABLE);
        }

        WebScraperConnectionProvider provider = providerOpt.get();

        try {
            String userAgent = StringUtils.hasText(request.userAgent()) ? request.userAgent()
                    : UserAgentUtil.getRandomUserAgent();

            connectionOpt = provider.openConnection(request.url(), userAgent, request.cookies());

            if (connectionOpt.isPresent()) {
                int responseCode = connectionOpt.get().getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return new WebScraperResponse(request, null,
                            HttpStatusCode.valueOf(responseCode), SiteScrapStatus.SUCCESS);
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connectionOpt.get().getInputStream()))) {

                    StringJoiner sj = new StringJoiner(System.lineSeparator());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sj.add(line);
                    }

                    return new WebScraperResponse(request, sj.toString(),
                            HttpStatusCode.valueOf(responseCode), SiteScrapStatus.SUCCESS);
                }
            }

            return new WebScraperResponse(request, null, null,
                    SiteScrapStatus.CONNECTION_UNAVAILABLE);
        } catch (IOException e) {
            throw new SiteConnectionException(
                    String.format("Provider %s could not build the response for request %s",
                            provider.getClass().getSimpleName(), request), e);
        } finally {
            connectionOpt.ifPresent(provider::closeConnection);
        }
    }
}
