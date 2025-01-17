package pt.zerodseis.services.webscraper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.http.HttpStatus;
import pt.zerodseis.services.webscraper.runners.SiteScrapStatus;

public record WebScraperResponseDto(
        @Schema(description = "The identifier for request")
        Integer requestId,
        @Schema(description = "The content returned from scraped website")
        String content,
        @Schema(description = "The status code returned from scraped website")
        HttpStatus statusCode,
        @Schema(description = "The status of the request to scrap site")
        SiteScrapStatus siteScrapStatus) implements
        Serializable {

    @Serial
    private static final long serialVersionUID = -3708141780559962926L;
}
