package pt.zerodseis.services.webscraper.mapper;

import java.net.HttpCookie;
import org.mapstruct.Mapper;
import pt.zerodseis.services.webscraper.dto.HttpCookieDto;

@Mapper
public interface HttpCookieDtoMapper {

    default HttpCookieDto toDto(HttpCookie cookie) {
        if (cookie == null) {
            return null;
        }

        return new HttpCookieDto(cookie.getName(), cookie.getValue());
    }

    default HttpCookie fromDto(HttpCookieDto dto) {
        if (dto == null) {
            return null;
        }

        return new HttpCookie(dto.name(), dto.value());
    }
}
