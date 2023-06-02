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
import java.util.concurrent.atomic.AtomicBoolean;
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
        value = "proxy.tor.enabled",
        havingValue = "true"
)
public class TorProxy implements WebScraperProxy {

    private final Map<UUID, HttpURLConnection> activeConnections;
    private final AtomicBoolean isRestartingService;
    private final String restartScripPath;
    private final Proxy proxy;
    private final AtomicReference<InetAddress> ipAddr;


    public TorProxy(
            @Value("${proxy.tor.hostname}") String hostname,
            @Value("${proxy.tor.port}") int port,
            @Value("${proxy.tor.restart.script.path}") String restartScripPath) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.isRestartingService = new AtomicBoolean(false);
        this.restartScripPath = restartScripPath;
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
        if (isRestartingService.get()) {
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
            if (isRestartingService.compareAndSet(false, true)) {
                try {
                    Process process = new ProcessBuilder("/bin/sh", restartScripPath).start();
                    process.waitFor();
                    isRestartingService.set(false);
                    ipAddr.set(IpAddressUtil.getExternalIpAddress(this));
                    log.info("External IP was updated to: " + getIp().getHostAddress());
                } catch (InterruptedException | IOException e) {
                    throw new RenewExternalIpAddressException(
                            "Could not renew TorProxy IP", e);
                }
            }
        }
    }
}
