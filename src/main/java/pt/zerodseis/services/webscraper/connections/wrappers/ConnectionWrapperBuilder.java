package pt.zerodseis.services.webscraper.connections.wrappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;
import pt.zerodseis.services.webscraper.connections.HTTPConnectionContentType;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;


@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ConnectionWrapperBuilder {

    private URL url;
    private Proxy proxy;
    private Map<String, List<Object>> requestProperties;
    private HTTPConnectionContentType contentType;

    public ConnectionWrapperBuilder url(URL url) {
        this.url = url;
        return this;
    }

    public ConnectionWrapperBuilder proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }
    public ConnectionWrapperBuilder requestProperties(Map<String, List<Object>> requestProperties) {
        this.requestProperties = requestProperties;
        return this;
    }

    public ConnectionWrapperBuilder contentType(HTTPConnectionContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public URLConnectionWrapper build() throws IOException {
        validate();

        if (HTTPConnectionContentType.DYNAMIC_HTML_PAGE.equals(contentType)) {
            return new SeleniumURLConnectionWrapper(url, proxy, requestProperties);
        }

        return new DefaultURLConnectionWrapper(url, proxy, requestProperties);
    }

    private void validate() {
        Validate.notNull(url, "url is required");
        Validate.notNull(proxy, "proxy is required");
        Validate.notNull(requestProperties, "requestProperties is required");
    }
}
