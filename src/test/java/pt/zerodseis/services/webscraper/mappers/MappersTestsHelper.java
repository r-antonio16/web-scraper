package pt.zerodseis.services.webscraper.mappers;

import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.runners.ScrapTaskStatus;
import pt.zerodseis.services.webscraper.web.models.HttpCookieDto;
import pt.zerodseis.services.webscraper.web.models.WebScraperRequestDto;
import pt.zerodseis.services.webscraper.web.models.WebScraperResponseDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappersTestsHelper {

    public static HttpCookie getHttpCookie() {
        return new HttpCookie("sessionId", "12");
    }

    public static HttpCookieDto getHttpCookieDto() {
        return new HttpCookieDto("sessionId", "12");
    }

    public static WebScraperRequest getWebScraperRequest() {
        try {
            return new WebScraperRequest(1, URI.create("https://test.com").toURL(),
                    new HttpCookie[]{getHttpCookie()}, "user-agent");
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static WebScraperRequestDto getWebScraperRequestDto() {
        return new WebScraperRequestDto(1, "https://test.com", List.of(getHttpCookieDto()),
                "user-agent");
    }

    public static List<WebScraperRequestDto> getWebScraperRequestListDto(int itemsToGen) {
        List<WebScraperRequestDto> list = new LinkedList<>();

        for (int i = 0; i < itemsToGen; i++) {
            list.add(new WebScraperRequestDto(i, "https://test.com/" + i,
                    List.of(getHttpCookieDto()),
                    "user-agent-" + i));
        }

        return list;
    }

    public static WebScraperResponse getWebScraperResponse() {
        return new WebScraperResponse(getWebScraperRequest(), "content", HttpStatus.OK,
                ScrapTaskStatus.REQUEST_SUCCESS);
    }

    public static WebScraperResponseDto getWebScraperResponseDto() {
        return new WebScraperResponseDto(1, "content", HttpStatus.OK,
                ScrapTaskStatus.REQUEST_SUCCESS);
    }

    public static List<WebScraperResponseDto> getWebScraperResponseListDto(int itemsToGen) {
        List<WebScraperResponseDto> list = new LinkedList<>();

        for (int i = 0; i < itemsToGen; i++) {
            list.add(new WebScraperResponseDto(i, "content " + i, HttpStatus.OK,
                    ScrapTaskStatus.REQUEST_SUCCESS));
        }

        return list;
    }

    public static List<WebScraperResponse> getWebScraperResponse(int itemsToGen) {
        List<WebScraperResponse> list = new LinkedList<>();

        for (int i = 0; i < itemsToGen; i++) {
            try {
                WebScraperRequest request = new WebScraperRequest(i,
                        URI.create("https://test.com/" + i).toURL(),
                        new HttpCookie[]{getHttpCookie()}, "user-agent." + i);
                list.add(new WebScraperResponse(request, "content", HttpStatus.OK,
                        ScrapTaskStatus.REQUEST_SUCCESS));
            } catch (MalformedURLException ignored) {
            }
        }

        return list;
    }
}
