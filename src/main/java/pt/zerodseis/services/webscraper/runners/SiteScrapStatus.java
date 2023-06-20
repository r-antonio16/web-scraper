package pt.zerodseis.services.webscraper.runners;

public enum SiteScrapStatus {
    SUCCESS,
    REQUEST_TIMEOUT,
    INTERRUPTED,
    FAIL,
    CONNECTION_UNAVAILABLE,
    PROVIDER_UNAVAILABLE
}