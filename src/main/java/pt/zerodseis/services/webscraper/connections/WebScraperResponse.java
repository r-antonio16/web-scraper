package pt.zerodseis.services.webscraper.connections;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public interface WebScraperResponse {

    WebScraperRequest getRequest();

    String getContent();

    HttpStatusCode getStatusCode();
}
