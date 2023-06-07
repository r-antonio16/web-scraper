package pt.zerodseis.services.webscraper.connections;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.concurrent.TimeUnit;
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
public class TorConnectionProvider extends AbstractConnectionProvider {

    private final String restartScriptPath;
    private final long waitForRestartScriptTimeout;
    private final TimeUnit waitForRestartScriptUnit;
    private final String commandShell;

    public TorConnectionProvider(
            @Value("${connection.provider.tor.hostname}") String hostname,
            @Value("${connection.provider.tor.port}") int port,
            @Value("${connection.provider.tor.max.active.connections}") int maxActiveConnections,
            @Value("${connection.provider.tor.restart.script.path}") String restartScriptPath,
            @Value("${connection.provider.tor.wait.for.restart.script.timeout}") long waitForRestartScriptTimeout,
            @Value("${connection.provider.tor.wait.for.restart.script.unit}") TimeUnit waitForRestartScriptUnit) {
        super(maxActiveConnections, new Proxy(Type.SOCKS, new InetSocketAddress(hostname, port)));
        this.restartScriptPath = restartScriptPath;
        this.waitForRestartScriptTimeout = waitForRestartScriptTimeout;
        this.waitForRestartScriptUnit = waitForRestartScriptUnit;
        this.commandShell = getCommandShellByOS();

        if (getIp() != null) {
            log.info("External IP: " + getIp().getHostAddress());
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

    private String getCommandShellByOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "cmd.exe" : "/bin/sh";
    }
}
