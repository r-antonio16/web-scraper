package pt.zerodseis.services.webscraper.connections;

import java.net.Proxy;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DefaultConnectionProvider extends AbstractConnectionProvider {

    public DefaultConnectionProvider(
            @Value("${connection.provider.default.max.active.connections}") int maxActiveConnections) {
        super(maxActiveConnections, Proxy.NO_PROXY);

        if (getIp() != null) {
            log.info("External IP: " + getIp().getHostAddress());
        }
    }
}
