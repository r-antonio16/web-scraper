package pt.zerodseis.services.webscraper.web.models;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

public record HttpCookieDto(@NotBlank String name, @NotBlank String value) implements Serializable {

    @Serial
    private static final long serialVersionUID = -996789351026264854L;
}
