package pt.zerodseis.services.webscraper.connections;

import jakarta.servlet.http.Cookie;
import java.net.URI;

public interface WebScraperRequest {

    URI getUrl();
    Cookie[] getCookies();

    String getUserAgent();

}
