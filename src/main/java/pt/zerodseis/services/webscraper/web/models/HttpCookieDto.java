package pt.zerodseis.services.webscraper.web.models;

import java.io.Serial;
import java.io.Serializable;

public record HttpCookieDto(String name, String value) implements Serializable {

    @Serial
    private static final long serialVersionUID = -996789351026264854L;
}
