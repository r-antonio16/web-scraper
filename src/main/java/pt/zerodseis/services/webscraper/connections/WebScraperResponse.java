package pt.zerodseis.services.webscraper.connections;

import org.springframework.http.HttpStatusCode;

public record WebScraperResponse(WebScraperRequest request, String content,
                                 HttpStatusCode statusCode) {

}
