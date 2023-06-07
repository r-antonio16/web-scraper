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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final AtomicInteger activeConnectionsCounter;
    private final int maxActiveConnections;
    private final String restartScriptPath;
    private final long waitForRestartScriptTimeout;
    private final TimeUnit waitForRestartScriptUnit;
    private final String commandShell;
    private final AtomicReference<WebScraperConnectionProviderStatus> status;
    private final Proxy proxy;
    private final AtomicReference<InetAddress> ipAddr;

    public TorConnectionProvider(
            @Value("${connection.provider.tor.hostname}") String hostname,
            @Value("${connection.provider.tor.port}") int port,
            @Value("${connection.provider.tor.max.active.connections}") int maxActiveConnections,
            @Value("${connection.provider.tor.restart.script.path}") String restartScriptPath,
            @Value("${connection.provider.tor.wait.for.restart.script.timeout}") long waitForRestartScriptTimeout,
            @Value("${connection.provider.tor.wait.for.restart.script.unit}") TimeUnit waitForRestartScriptUnit) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.activeConnectionsCounter = new AtomicInteger();
        this.maxActiveConnections = maxActiveConnections;
        this.restartScriptPath = restartScriptPath;
        this.waitForRestartScriptTimeout = waitForRestartScriptTimeout;
        this.waitForRestartScriptUnit = waitForRestartScriptUnit;
        this.status = new AtomicReference<>(WebScraperConnectionProviderStatus.UP);
        this.proxy = new Proxy(Type.SOCKS, new InetSocketAddress(hostname, port));
        this.ipAddr = new AtomicReference<>(IpAddressUtil.getExternalIpAddress(this));
        this.commandShell = getCommandShellByOS();

        if (getIp() != null) {
            log.info("External IP: " + getIp().getHostAddress());
        } else {
            this.status.set(WebScraperConnectionProviderStatus.DOWN);
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
        if (activeConnectionsCounter.get() == 0) {
            if (status.compareAndSet(WebScraperConnectionProviderStatus.UP,
                    WebScraperConnectionProviderStatus.RESTARTING)
                    || status.compareAndSet(WebScraperConnectionProviderStatus.DOWN,
                    WebScraperConnectionProviderStatus.RESTARTING)) {
                try {
                    Process process = new ProcessBuilder(commandShell, restartScriptPath).start();
                    process.waitFor(waitForRestartScriptTimeout, waitForRestartScriptUnit);
                    if (0 == process.exitValue()) {
                        status.set(WebScraperConnectionProviderStatus.UP);
                        ipAddr.set(IpAddressUtil.getExternalIpAddress(this));
                        log.info("External IP updated to: " + getIp().getHostAddress());
                    } else {
                        status.set(WebScraperConnectionProviderStatus.DOWN);
                    }
                } catch (Exception e) {
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

    private String getCommandShellByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "cmd.exe" : "/bin/sh";
    }
}
