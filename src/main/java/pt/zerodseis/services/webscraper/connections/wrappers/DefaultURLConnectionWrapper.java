package pt.zerodseis.services.webscraper.connections.wrappers;

import pt.zerodseis.services.webscraper.exceptions.SiteConnectionInitException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DefaultURLConnectionWrapper implements URLConnectionWrapper {

    private final HttpURLConnection connection;

    public DefaultURLConnectionWrapper(URL url, Proxy proxy, Map<String, List<Object>> requestProperties) {
        try {
            this.connection = (HttpURLConnection) url.openConnection(proxy);
            boolean cookiesAdded = false;

            for (Map.Entry<String, List<Object>> requestProperty : requestProperties.entrySet()) {
                for (Object requestPropertyValue : requestProperty.getValue()) {
                    if (requestProperty.getKey().equals("Cookie")) {
                        if (!cookiesAdded) {
                            List<Object> cookies = requestProperties.get("Cookie");
                            StringBuilder sb = new StringBuilder();

                            for (Object cookieObj : cookies) {
                                HttpCookie httpCookie = (HttpCookie) cookieObj;
                                sb.append(String.format("%s=%s; ", httpCookie.getName(), httpCookie.getValue()));
                            }

                            connection.setRequestProperty(requestProperty.getKey(), sb.toString());
                            cookiesAdded = true;
                        }
                    } else {
                        connection.setRequestProperty(requestProperty.getKey(), (String) requestPropertyValue);
                    }
                }
            }
        } catch (Exception ex) {
            throw new SiteConnectionInitException("Could not initialize DefaultURLConnectionWrapper", ex);
        }
    }

    @Override
    public void connect() throws IOException {
        connection.connect();
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    @Override
    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }
}
