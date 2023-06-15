package pt.zerodseis.services.webscraper.connections;

import org.springframework.http.HttpStatusCode;
import pt.zerodseis.services.webscraper.runners.ScrapTaskStatus;

public record WebScraperResponse(WebScraperRequest request, String content,
                                 HttpStatusCode statusCode, ScrapTaskStatus scrapTaskStatus) {

}
