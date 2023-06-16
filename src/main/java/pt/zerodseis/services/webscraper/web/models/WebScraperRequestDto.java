package pt.zerodseis.services.webscraper.web.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record WebScraperRequestDto(Integer id, String url, List<HttpCookieDto> cookies,
                                   String userAgent) implements Serializable {

    @Serial
    private static final long serialVersionUID = -3708141780559962926L;
}

