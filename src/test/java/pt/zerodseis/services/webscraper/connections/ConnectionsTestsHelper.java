package pt.zerodseis.services.webscraper.connections;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectionsTestsHelper {

    static Runnable getOpenConnectionRunnable(WebScraperConnectionProvider provider, URL url,
            LinkedBlockingQueue<Optional<HTTPConnection>> connections) {
        return () -> {
            try {
                connections.add(provider.openConnection(url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    static Runnable getCloseConnectionRunnable(WebScraperConnectionProvider provider,
            HTTPConnection connection) {
        return () -> provider.closeConnection(connection);
    }

    static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    static WebScraperConnectionProvider getProviderInstance(String clazz,
            int maxActiveConnections) {

        if (clazz.equals(DefaultConnectionProvider.class.getSimpleName())) {
            return new DefaultConnectionProvider(maxActiveConnections);
        }

        if (clazz.equals(TorConnectionProvider.class.getSimpleName())) {
            return new TorConnectionProvider("127.0.0.1", 5000, maxActiveConnections, "", 1,
                    TimeUnit.SECONDS);
        }

        return null;
    }

    static Class<? extends WebScraperConnectionProvider> getClassType(
            String clazz) {

        if (clazz.equals(DefaultConnectionProvider.class.getSimpleName())) {
            return DefaultConnectionProvider.class;
        }

        if (clazz.equals(TorConnectionProvider.class.getSimpleName())) {
            return TorConnectionProvider.class;
        }

        return null;
    }
}
