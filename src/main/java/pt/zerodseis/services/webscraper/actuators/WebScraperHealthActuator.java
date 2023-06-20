package pt.zerodseis.services.webscraper.actuators;

import java.util.List;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import pt.zerodseis.services.webscraper.connections.WebScraperConnectionProvider;

@Component
public class WebScraperHealthActuator implements HealthIndicator {

    private final List<WebScraperConnectionProvider> providers;

    public WebScraperHealthActuator(List<WebScraperConnectionProvider> providers) {
        this.providers = providers;
    }

    @Override
    public Health health() {
        if (isHealthy()) {
            return Health.up().build();
        } else {
            return Health.down().build();
        }
    }

    private boolean isHealthy() {
        return providers.stream().anyMatch(p -> p.score() > 0);
    }
}
