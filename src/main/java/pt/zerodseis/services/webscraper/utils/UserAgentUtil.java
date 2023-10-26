package pt.zerodseis.services.webscraper.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pt.zerodseis.services.webscraper.exceptions.ReadUserAgentJsonException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAgentUtil {

    private static final List<UserAgent> desktopUAs = readUAJson("desktop");
    private static final List<UserAgent> mobileUAs = readUAJson("mobile");

    public static String getRandomUserAgent() {
        Random random = new Random();
        List<String> randomizedList = new ArrayList<>();
        List<UserAgent> uaDeviceType = random.nextDouble() < 0.52d ? desktopUAs : mobileUAs;

        for (UserAgent userAgent : uaDeviceType) {
            double rand = random.nextDouble();
            if (rand <= userAgent.pct()) {
                randomizedList.add(userAgent.ua());
            }
        }

        int randomIndex = random.nextInt(randomizedList.size());
        return randomizedList.get(randomIndex);
    }

    protected static List<UserAgent> readUAJson(String deviceType) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = UserAgentUtil.class.getResourceAsStream(String.format("/user-agents/%s-common" +
                    "-ua.json", deviceType));
            return objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ReadUserAgentJsonException(
                    String.format("Could not load %s user agents from json file", deviceType), e);
        }
    }

    protected record UserAgent(double pct, String ua) {
    }
}
