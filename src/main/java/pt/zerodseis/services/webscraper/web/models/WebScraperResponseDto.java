package pt.zerodseis.services.webscraper.web.models;

import java.io.Serial;
import java.io.Serializable;
import org.springframework.http.HttpStatusCode;
import pt.zerodseis.services.webscraper.runners.SiteScrapStatus;

public record WebScraperResponseDto(Integer requestId, String content,
                                    HttpStatusCode statusCode, SiteScrapStatus siteScrapStatus) implements
        Serializable {

    @Serial
    private static final long serialVersionUID = -3708141780559962926L;
}
