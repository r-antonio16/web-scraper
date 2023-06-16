package pt.zerodseis.services.webscraper.connections;

import java.net.HttpCookie;
import java.net.URL;


public record WebScraperRequest(Integer id, URL url, HttpCookie[] cookies, String userAgent) {

}
