package pt.zerodseis.services.webscraper.runners;

import java.util.List;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;

public interface WebScraperRunner {

    WebScraperResponse scrapSite(WebScraperRequest request);

    List<WebScraperResponse> scrapSites(List<WebScraperRequest> requests);
}
