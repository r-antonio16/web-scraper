package pt.zerodseis.services.webscraper.connections;

import java.net.Proxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultConnectionProvider extends AbstractConnectionProvider {

    public DefaultConnectionProvider(
            @Value("${connection.provider.default.max.active.connections}") int maxActiveConnections) {
        super(maxActiveConnections, Proxy.NO_PROXY);
    }
}
