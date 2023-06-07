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
public class ProviderTestHelper {

    public static Runnable getOpenConnectionRunnable(WebScraperConnectionProvider provider, URL url,
            LinkedBlockingQueue<Optional<HTTPConnection>> connections) {
        return () -> {
            try {
                connections.add(provider.openConnection(url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Runnable getCloseConnectionRunnable(WebScraperConnectionProvider provider,
            HTTPConnection connection) {
        return () -> provider.closeConnection(connection);
    }

    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
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
}
