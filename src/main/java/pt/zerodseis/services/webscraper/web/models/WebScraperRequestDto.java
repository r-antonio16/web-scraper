package pt.zerodseis.services.webscraper.web.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record WebScraperRequestDto(
        @Schema(description = "The identifier for request")
        @NotNull
        Integer id,
        @Schema(description = "The Url of website to be scraped")
        @NotBlank
        @URL
        String url,
        @Schema(description = "Cookies sent to the website to be scraped")
        List<@Valid HttpCookieDto> cookies,
        @Schema(description = "User Agent sent to the website to be scraped")
        String userAgent) implements Serializable {

    @Serial
    private static final long serialVersionUID = -3708141780559962926L;
}

