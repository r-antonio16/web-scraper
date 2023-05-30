package pt.zerodseis.services.webscraper.templates;

import java.util.List;

public interface AdsListTemplate extends WebScraperTemplate {

    int getResultsPerPage();
    long getTotalResults();
    int getPage();
    List<Ad> getAds();
}
