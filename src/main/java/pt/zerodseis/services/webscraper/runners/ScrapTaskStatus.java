package pt.zerodseis.services.webscraper.runners;

public enum ScrapTaskStatus {
    REQUEST_SUCCESS,
    REQUEST_TIMEOUT,
    REQUEST_INTERRUPTED,
    REQUEST_ERROR,
    CONNECTION_UNAVAILABLE,
    PROVIDER_UNAVAILABLE
}