package pt.zerodseis.services.webscraper.connections;

import java.net.InetAddress;

public interface WebScraperConnection {

    InetAddress getIp();
    long getActiveConnections();
    boolean renewIP();
}
