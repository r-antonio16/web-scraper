package pt.zerodseis.services.webscraper.connections.wrappers;

import java.io.IOException;
import java.io.InputStream;

public interface URLConnectionWrapper {

    void connect() throws IOException;

    void disconnect();

    int getResponseCode() throws IOException;

    InputStream getInputStream() throws IOException;

    static ConnectionWrapperBuilder builder() {
        return new ConnectionWrapperBuilder();
    }
}
