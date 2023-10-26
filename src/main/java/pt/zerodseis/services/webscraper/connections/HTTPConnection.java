package pt.zerodseis.services.webscraper.connections;

import pt.zerodseis.services.webscraper.connections.wrappers.URLConnectionWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public record HTTPConnection(UUID uuid, URLConnectionWrapper connection) {


    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }
}
