package pt.zerodseis.services.webscraper.connections;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;

public interface WebScraperProxy {

    InetAddress getIp();
    int getActiveConnections();

    Optional<HTTPConnection> openConnection(URL url) throws IOException;

    void closeConnection(HTTPConnection connection);

    void renewIp();
}
