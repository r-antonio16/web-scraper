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

    private static final int PERCENTAGE_FACTOR = 100;
    private static final int DEFAULT_SCORE = 0;

    protected final Map<UUID, HttpURLConnection> activeConnections;
    protected final AtomicInteger freeConnections;
    protected final AtomicInteger totalConnections;
    protected final AtomicInteger failedConnections;
    protected final int maxActiveConnections;
    protected final AtomicReference<WebScraperConnectionProviderStatus> status;
    protected final Proxy proxy;
    protected final AtomicReference<InetAddress> ipAddr;

    public AbstractConnectionProvider(int maxActiveConnections, Proxy proxy) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.freeConnections = new AtomicInteger(maxActiveConnections);
        this.totalConnections = new AtomicInteger();
        this.failedConnections = new AtomicInteger();
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
        return freeConnections.get();
    }

    @Override
    public boolean isActiveConnectionsLimitReached() {
        return freeConnections.get() < 1;
    }

    @Override
    public Optional<HTTPConnection> openConnection(URL url, String userAgent, HttpCookie... cookies)
            throws IOException {
        if (!WebScraperConnectionProviderStatus.UP.equals(status.get())
                || isActiveConnectionsLimitReached()) {
            return Optional.empty();
        }

        freeConnections.decrementAndGet();

        totalConnections.incrementAndGet();

        boolean failedConnectionAlreadyIncremented = false;

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);

            setConnectionProperties(connection, userAgent, cookies);

            connection.connect();

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                failedConnections.incrementAndGet();
            }

            UUID uuid = UUID.randomUUID();
            activeConnections.put(uuid, connection);
            return Optional.of(new HTTPConnection(uuid, connection));

        } catch (IOException e) {
            if (!failedConnectionAlreadyIncremented) {
                failedConnections.incrementAndGet();
            }
            throw e;
        }
    }

    @Override
    public Optional<HTTPConnection> openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    @Override
    public void closeConnection(HTTPConnection connection) {
        if (activeConnections.containsKey(connection.uuid())) {
            freeConnections.incrementAndGet();
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
        if (!isConnectionProviderUp()) {
            return DEFAULT_SCORE;
        }

        if (hasNoFailedConnections()) {
            return freeConnections.get() * PERCENTAGE_FACTOR;
        }

        double successConnectionsRate = calculateSuccessConnectionsRate();
        return calculateScoreWithSuccessConnectionsRate(successConnectionsRate);
    }

    @PreDestroy
    protected void destroy() {
        if (getFreeConnections() < maxActiveConnections) {
            activeConnections.forEach((uuid, conn) -> {
                conn.disconnect();
            });

            activeConnections.clear();
            freeConnections.set(maxActiveConnections);
        }
    }

    private boolean isConnectionProviderUp() {
        return WebScraperConnectionProviderStatus.UP.equals(status.get());
    }

    private boolean hasNoFailedConnections() {
        return failedConnections.get() == 0;
    }

    private double calculateSuccessConnectionsRate() {
        int totalConnections = this.totalConnections.get();
        int successfulConnections = totalConnections - failedConnections.get();

        if (totalConnections == 0) {
            return 0.0;
        }

        return (double) successfulConnections / totalConnections;
    }

    private int calculateScoreWithSuccessConnectionsRate(double successConnectionsRate) {
        return (int) (freeConnections.get() * successConnectionsRate * PERCENTAGE_FACTOR);
    }

    private void setConnectionProperties(HttpURLConnection connection, String userAgent,
            HttpCookie... cookies) {
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
    }
}
