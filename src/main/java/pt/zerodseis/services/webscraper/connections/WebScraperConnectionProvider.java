package pt.zerodseis.services.webscraper.connections;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;

public interface WebScraperConnectionProvider {

    InetAddress getIp();

    int getFreeConnections();

    boolean isActiveConnectionsLimitReached();

    Optional<HTTPConnection> openConnection(URL url, HTTPConnectionContentType contentType, String userAgent,
            HttpCookie... cookies) throws IOException;

    Optional<HTTPConnection> openConnection(URL url) throws IOException;

    void closeConnection(HTTPConnection connection);

    void renewIp();

    WebScraperConnectionProviderStatus getStatus();

    int score();
}
