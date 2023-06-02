package pt.zerodseis.services.webscraper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pt.zerodseis.services.webscraper.connections.HTTPConnection;
import pt.zerodseis.services.webscraper.connections.WebScraperConnectionProvider;
import pt.zerodseis.services.webscraper.exceptions.GetExternalIpAddressException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IpAddressUtil {

    private final static String WEBSITE_TO_GET_EXTERNAL_IP = getWebsiteToGetExternalIp();

    public static InetAddress getExternalIpAddress(WebScraperConnectionProvider provider) {
        Optional<HTTPConnection> connectionOpt = Optional.empty();

        try {
            URL url = URI.create(WEBSITE_TO_GET_EXTERNAL_IP).toURL();

            connectionOpt = provider.openConnection(url);

            if (connectionOpt.isPresent()) {
                int responseCode = connectionOpt.get().getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new GetExternalIpAddressException(
                            "Could not get a success response from " + url
                                    + " while consulting the external IP address of "
                                    + provider.getClass());
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connectionOpt.get().getInputStream()))) {
                    return Optional.ofNullable(reader.readLine())
                            .map(IpAddressUtil::inetAddressByName)
                            .orElse(null);
                }
            }

            return null;
        } catch (IOException e) {
            throw new GetExternalIpAddressException(
                    "Could not get the external IP address of "
                            + provider.getClass(), e);
        } finally {
            if (provider != null && connectionOpt.isPresent()) {
                provider.closeConnection(connectionOpt.get());
            }
        }
    }

    private static InetAddress inetAddressByName(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new GetExternalIpAddressException(
                    "Could not get InetAddress from host: %s" + host, e);
        }
    }

    private static String getWebsiteToGetExternalIp() {
        String envValue = System.getenv("WEBSITE_TO_GET_EXTERNAL_IP");

        if (envValue == null) {
            return "https://api.ipify.org";
        }

        return envValue;
    }
}
