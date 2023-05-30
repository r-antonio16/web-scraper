package pt.zerodseis.services.webscraper.templates;

import java.util.List;

public interface AdsListTemplate<T> extends WebScraperTemplate {

    int getResultsPerPage();
    long getTotalResults();
    int getPage();
    List<T> getAds();
}
