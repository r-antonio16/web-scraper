package pt.zerodseis.services.webscraper.connections;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.HttpCookie;
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
import org.springframework.util.StringUtils;
import pt.zerodseis.services.webscraper.utils.IpAddressUtil;

@Log4j2
abstract class AbstractConnectionProvider implements WebScraperConnectionProvider {

    protected final Map<UUID, HttpURLConnection> activeConnections;
    protected final AtomicInteger freeConnectionsCounter;
    protected final int maxActiveConnections;
    protected final AtomicReference<WebScraperConnectionProviderStatus> status;
    protected final Proxy proxy;
    protected final AtomicReference<InetAddress> ipAddr;

    public AbstractConnectionProvider(int maxActiveConnections, Proxy proxy) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.freeConnectionsCounter = new AtomicInteger(maxActiveConnections);
        this.status = new AtomicReference<>(WebScraperConnectionProviderStatus.UP);
        this.maxActiveConnections = maxActiveConnections;
        this.proxy = proxy;
        this.ipAddr = new AtomicReference<>(IpAddressUtil.getExternalIpAddress(this));

        if (getIp() == null) {
            this.status.set(WebScraperConnectionProviderStatus.DOWN);
        } else {
            log.info(this.getClass().getSimpleName() + " external IP is "
                    + getIp().getHostAddress());
        }
    }

    @Override
    public InetAddress getIp() {
        return ipAddr.get();
    }

    @Override
    public int getFreeConnections() {
        return freeConnectionsCounter.get();
    }

    @Override
    public boolean isActiveConnectionsLimitReached() {
        return freeConnectionsCounter.get() < 1;
    }

    @Override
    public Optional<HTTPConnection> openConnection(URL url, String userAgent, HttpCookie... cookies)
            throws IOException {
        if (!WebScraperConnectionProviderStatus.UP.equals(status.get())
                || isActiveConnectionsLimitReached()) {
            return Optional.empty();
        }

        freeConnectionsCounter.decrementAndGet();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);

        if (StringUtils.hasText(userAgent)) {
            connection.setRequestProperty("User-Agent", userAgent);
        }

        if (cookies != null && cookies.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (HttpCookie cookie : cookies) {
                sb.append(String.format("%s=%s; ", cookie.getName(), cookie.getValue()));
            }

            connection.setRequestProperty("Cookie", sb.toString());
        }

        connection.connect();
        UUID uuid = UUID.randomUUID();
        activeConnections.put(uuid, connection);
        return Optional.of(new HTTPConnection(uuid, connection));
    }

    @Override
    public Optional<HTTPConnection> openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    @Override
    public void closeConnection(HTTPConnection connection) {
        if (activeConnections.containsKey(connection.uuid())) {
            freeConnectionsCounter.incrementAndGet();
            HttpURLConnection httpURLConnection = activeConnections.remove(connection.uuid());
            httpURLConnection.disconnect();
        }
    }

    @Override
    public void renewIp() {
    }

    @Override
    public WebScraperConnectionProviderStatus getStatus() {
        return status.get();
    }

    @Override
    public int score() {
        if (!WebScraperConnectionProviderStatus.UP.equals(status.get())) {
            return 0;
        }

        return freeConnectionsCounter.get();
    }

    @PreDestroy
    protected void destroy() {
        if (getFreeConnections() < maxActiveConnections) {
            activeConnections.forEach((uuid, conn) -> {
                conn.disconnect();
            });

            activeConnections.clear();
            freeConnectionsCounter.set(maxActiveConnections);
        }
    }
}
