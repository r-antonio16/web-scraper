package pt.zerodseis.services.webscraper.web.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

public record HttpCookieDto(

        @Schema(description = "The name for cookie")
        @NotBlank String name,

        @Schema(description = "The value for cookie")
        @NotBlank String value) implements Serializable {

    @Serial
    private static final long serialVersionUID = -996789351026264854L;
}
