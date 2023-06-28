package pt.zerodseis.services.webscraper.connections.manager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import pt.zerodseis.services.webscraper.connections.WebScraperConnectionProvider;

@Component
public class WebScraperConnectionProviderManager {

    private final List<WebScraperConnectionProvider> providers;

    public WebScraperConnectionProviderManager(List<WebScraperConnectionProvider> providers) {
        this.providers = providers;
    }

    public synchronized Optional<WebScraperConnectionProvider> electProvider() {
        return providers.stream()
                .max(Comparator.comparing(WebScraperConnectionProvider::score))
                .filter(p -> p.score() > 0)
                .stream().findFirst();
    }
}
