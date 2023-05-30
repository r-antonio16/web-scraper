package pt.zerodseis.services.webscraper.processors;

import pt.zerodseis.services.webscraper.templates.WebScraperTemplate;

public interface WebContentProcessor {

    WebScraperTemplate generateTemplate();
}
