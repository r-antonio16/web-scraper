package pt.zerodseis.services.webscraper.connections.wrappers;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.springframework.util.CollectionUtils;
import pt.zerodseis.services.webscraper.connections.RequestPropertiesConstants;
import pt.zerodseis.services.webscraper.exceptions.SiteConnectionInitException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.net.Proxy.Type.SOCKS;

public class SeleniumURLConnectionWrapper implements URLConnectionWrapper {

    private final WebDriver driver;
    private final URL url;
    private final Proxy proxy;
    private final List<Cookie> cookies;

    public SeleniumURLConnectionWrapper(URL url, Proxy proxy, Map<String, List<Object>> requestProperties) {
        try {
            this.driver = initWebDriver(proxy);
            this.url = url;
            this.proxy = proxy;
            this.cookies = new ArrayList<>();
            buildCookies(requestProperties);
        } catch (Exception ex) {
            throw new SiteConnectionInitException("Could not initialize SeleniumURLConnectionWrapper", ex);
        }
    }

    private void buildCookies(Map<String, List<Object>> requestProperties) {
        if (requestProperties.containsKey(RequestPropertiesConstants.COOKIE_PROP)) {
            List<Object> cookies = requestProperties.get(RequestPropertiesConstants.COOKIE_PROP);
            for (Object cookieObj : cookies) {
                HttpCookie httpCookie = (HttpCookie) cookieObj;

                Date expiry = httpCookie.getMaxAge() > 0 ? new Date(httpCookie.getMaxAge()) : null;

                Cookie cookie = new Cookie(
                        httpCookie.getName(),
                        httpCookie.getValue(),
                        hostToDomain(url.getHost()),
                        httpCookie.getPath(),
                        expiry
                );

                this.cookies.add(cookie);
            }
        }
    }

    private String hostToDomain(String host) {
        String domain = host;

        if (domain != null && domain.startsWith("www.")) {
            domain = domain.substring(4);
        }

        return domain;
    }

    private void addProxyProps(Proxy proxy, FirefoxProfile profile) {
        if (SOCKS.equals(proxy.type())) {
            InetSocketAddress address = (InetSocketAddress) proxy.address();

            profile.setPreference("network.proxy.type", 1);
            profile.setPreference("network.proxy.socks", address.getHostName());
            profile.setPreference("network.proxy.socks_port", address.getPort());
            profile.setPreference("network.proxy.socks_version", 5);
            profile.setPreference("network.proxy.socks_remote_dns", true);
        }
    }

    private WebDriver initWebDriver(Proxy proxy) {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("permissions.default.image", 2);
        profile.setPreference("network.http.throttle.enable", false);
        profile.setPreference("network.http.throttle.suspendEvents", false);
        profile.setPreference("browser.cache.disk.enable", true);
        profile.setPreference("browser.cache.memory.enable", true);
        profile.setPreference("network.http.pipelining", true);
        profile.setPreference("network.http.proxy.pipelining", true);
        profile.setPreference("network.http.pipelining.maxrequests", 10);
        
        addProxyProps(proxy, profile);
        
        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");

        return new FirefoxDriver(options);
    }

    @Override
    public void connect() {
        driver.get(url.toString());

        if (!CollectionUtils.isEmpty(cookies)) {
            cookies.forEach(c -> driver.manage().addCookie(c));
            driver.navigate().refresh();
        }
    }

    @Override
    public void disconnect() {
        driver.quit();
    }

    @Override
    public int getResponseCode() throws IOException {
        HttpURLConnection cn = (HttpURLConnection) URI.create(driver.getCurrentUrl()).toURL().openConnection(proxy);
        cn.setRequestMethod("HEAD");
        cn.connect();
        int statusCode = cn.getResponseCode();
        cn.disconnect();

        return statusCode;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(driver.getPageSource().getBytes());
    }
}
