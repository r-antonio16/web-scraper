package pt.zerodseis.services.webscraper.connections;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.UUID;

public record HTTPConnection(UUID uuid, HttpURLConnection connection) {


    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }
}
