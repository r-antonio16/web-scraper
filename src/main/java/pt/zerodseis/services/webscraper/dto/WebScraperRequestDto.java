package pt.zerodseis.services.webscraper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import org.hibernate.validator.constraints.URL;
import pt.zerodseis.services.webscraper.connections.HTTPConnectionContentType;

public record WebScraperRequestDto(
        @Schema(description = "The identifier for the request")
        @NotNull
        Integer id,
        @Schema(description = "The Url of website to be scraped")
        @NotBlank
        @URL
        String url,
        @Schema(description = "Cookies sent to the website to be scraped")
        List<@Valid HttpCookieDto> cookies,
        @Schema(description = "User Agent sent to the website to be scraped")
        String userAgent,
        @Schema(description = "Content type (json, Dynamic page or Static page) of the url to be scraped")
        @NotNull
        HTTPConnectionContentType contentType) implements Serializable {

    @Serial
    private static final long serialVersionUID = -3708141780559962926L;
}

