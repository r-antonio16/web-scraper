package pt.zerodseis.services.webscraper.connections;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.log4j.Log4j2;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;

@Log4j2
abstract class AbstractConnectionProvider implements WebScraperConnectionProvider {

    protected final Map<UUID, HttpURLConnection> activeConnections;
    protected final AtomicInteger activeConnectionsCounter;
    protected final int maxActiveConnections;
    protected final AtomicReference<WebScraperConnectionProviderStatus> status;
    protected final Proxy proxy;
    protected final AtomicReference<InetAddress> ipAddr;

    public AbstractConnectionProvider(int maxActiveConnections, Proxy proxy) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.activeConnectionsCounter = new AtomicInteger();
        this.status = new AtomicReference<>(WebScraperConnectionProviderStatus.UP);
        this.maxActiveConnections = maxActiveConnections;
        this.proxy = proxy;
        this.ipAddr = new AtomicReference<>(IpAddressUtil.getExternalIpAddress(this));

        if (getIp() == null) {
            this.status.set(WebScraperConnectionProviderStatus.DOWN);
        } else {
            log.info(this.getClass().getSimpleName() + " external IP is " + getIp().getHostAddress());
        }
    }

    @Override
    public InetAddress getIp() {
        return ipAddr.get();
    }

    @Override
    public int getActiveConnections() {
        return activeConnectionsCounter.get();
    }

    @Override
    public boolean isActiveConnectionsLimitReached() {
        return activeConnectionsCounter.get() >= maxActiveConnections;
    }

    @Override
    public Optional<HTTPConnection> openConnection(URL url) throws IOException {
        if (!WebScraperConnectionProviderStatus.UP.equals(status.get())
                || isActiveConnectionsLimitReached()) {
            return Optional.empty();
        }

        activeConnectionsCounter.incrementAndGet();
        UUID uuid = UUID.randomUUID();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.connect();
        activeConnections.put(uuid, connection);
        return Optional.of(new HTTPConnection(uuid, connection));
    }

    @Override
    public void closeConnection(HTTPConnection connection) {
        if (activeConnections.containsKey(connection.uuid())) {
            HttpURLConnection httpURLConnection = activeConnections.get(connection.uuid());
            httpURLConnection.disconnect();
            activeConnections.remove(connection.uuid());
            activeConnectionsCounter.decrementAndGet();
        }
    }

    @Override
    public void renewIp() {
    }

    @Override
    public WebScraperConnectionProviderStatus getStatus() {
        return status.get();
    }
}
