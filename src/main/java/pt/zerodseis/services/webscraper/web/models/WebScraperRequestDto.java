package pt.zerodseis.services.webscraper.web.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import org.hibernate.validator.constraints.URL;

public record WebScraperRequestDto(@NotNull Integer id, @NotBlank @URL String url,
                                   List<@Valid HttpCookieDto> cookies,
                                   String userAgent) implements Serializable {

    @Serial
    private static final long serialVersionUID = -3708141780559962926L;
}

