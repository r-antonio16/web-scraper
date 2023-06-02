package pt.zerodseis.services.webscraper.connections;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pt.zerodseis.services.webscraper.exceptions.RenewExternalIpAddressException;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;

@Log4j2
@Component
@ConditionalOnProperty(
        value = "connection.provider.tor.enabled",
        havingValue = "true"
)
public class TorConnectionProvider implements WebScraperConnectionProvider {

    private final Map<UUID, HttpURLConnection> activeConnections;
    private final String restartScripPath;
    private final AtomicReference<WebScraperConnectionProviderStatus> status;
    private final Proxy proxy;
    private final AtomicReference<InetAddress> ipAddr;


    public TorConnectionProvider(
            @Value("${connection.provider.tor.hostname}") String hostname,
            @Value("${connection.provider.tor.port}") int port,
            @Value("${connection.provider.tor.restart.script.path}") String restartScripPath) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.restartScripPath = restartScripPath;
        this.status = new AtomicReference<>(WebScraperConnectionProviderStatus.UP);
        this.proxy = new Proxy(Type.SOCKS, new InetSocketAddress(hostname, port));
        this.ipAddr = new AtomicReference<>(IpAddressUtil.getExternalIpAddress(this));
        log.info("External IP: " + getIp().getHostAddress());
    }

    @Override
    public InetAddress getIp() {
        return ipAddr.get();
    }

    @Override
    public int getActiveConnections() {
        return activeConnections.size();
    }

    @Override
    public Optional<HTTPConnection> openConnection(URL url) throws IOException {
        if (!WebScraperConnectionProviderStatus.UP.equals(status.get())) {
            return Optional.empty();
        }

        UUID uuid = UUID.randomUUID();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        activeConnections.put(uuid, connection);
        return Optional.of(new HTTPConnection(uuid, connection));
    }

    @Override
    public void closeConnection(HTTPConnection connection) {
        if (activeConnections.containsKey(connection.uuid())) {
            HttpURLConnection httpURLConnection = activeConnections.get(connection.uuid());
            httpURLConnection.disconnect();
            activeConnections.remove(connection.uuid());
        }
    }

    @Override
    public void renewIp() {
        if (activeConnections.isEmpty()) {
            if (!WebScraperConnectionProviderStatus.RESTARTING.equals(status.get())) {
                status.set(WebScraperConnectionProviderStatus.RESTARTING);
                try {
                    Process process = new ProcessBuilder("/bin/sh", restartScripPath).start();
                    process.waitFor();
                    status.set(WebScraperConnectionProviderStatus.UP);
                    ipAddr.set(IpAddressUtil.getExternalIpAddress(this));
                    log.info("External IP updated to: " + getIp().getHostAddress());
                } catch (InterruptedException | IOException e) {
                    status.set(WebScraperConnectionProviderStatus.DOWN);
                    throw new RenewExternalIpAddressException(
                            "Could not renew IP for " + this.getClass(), e);
                }
            }
        }
    }

    @Override
    public WebScraperConnectionProviderStatus getStatus() {
        return status.get();
    }
}
