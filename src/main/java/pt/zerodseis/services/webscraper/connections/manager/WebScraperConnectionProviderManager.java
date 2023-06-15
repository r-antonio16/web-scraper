package pt.zerodseis.services.webscraper.connections.manager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import pt.zerodseis.services.webscraper.connections.WebScraperConnectionProvider;

@Component
public class WebScraperConnectionProviderManager {

    private final WebScraperConnectionProvider defaultProvider;
    private final List<WebScraperConnectionProvider> providers;

    public WebScraperConnectionProviderManager(List<WebScraperConnectionProvider> providers,
            WebScraperConnectionProvider defaultConnectionProvider) {
        this.defaultProvider = defaultConnectionProvider;
        this.providers = providers.stream()
                .filter(p -> !defaultProvider.getClass().equals(p.getClass()))
                .collect(Collectors.toList());
    }

    public synchronized Optional<WebScraperConnectionProvider> electProvider() {
        return providers.stream()
                .max(Comparator.comparing(WebScraperConnectionProvider::score))
                .filter(p -> p.score() > 1)
                .or(() -> Optional.ofNullable(defaultProvider))
                .filter(p -> p.score() > 0);
    }
}
