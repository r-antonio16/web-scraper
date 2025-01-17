package pt.zerodseis.services.webscraper.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.HttpCookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.zerodseis.services.webscraper.dto.HttpCookieDto;

@ExtendWith(SpringExtension.class)
public class HttpCookieDtoMapperTest {

    private final HttpCookieDtoMapper mapper = Mappers.getMapper(HttpCookieDtoMapper.class);

    @Test
    public void should_MapNonNullHttpCookieToHttpCookieDto_When_toDto_IsCalled() {
        HttpCookie httpCookie =  MappersTestsHelper.getHttpCookie();
        HttpCookieDto dto = mapper.toDto(httpCookie);

        assertNotNull(dto);
        assertEquals(httpCookie.getName(), dto.name());
        assertEquals(httpCookie.getValue(), dto.value());
    }

    @Test
    public void should_MapNullHttpCookieToNull_When_toDto_IsCalled() {
        HttpCookieDto dto = mapper.toDto(null);

        assertNull(dto);
    }

    @Test
    public void should_MapNonNullHttpCookieDtoToHttpCookie_When_fromDto_IsCalled() {
        HttpCookieDto dto = MappersTestsHelper.getHttpCookieDto();
        HttpCookie httpCookie = mapper.fromDto(dto);

        assertNotNull(dto);
        assertEquals(dto.name(), httpCookie.getName());
        assertEquals(dto.value(), httpCookie.getValue());
    }

    @Test
    public void should_MapNullHttpCookieDtoToNull_When_fromDto_IsCalled() {
        HttpCookie httpCookie = mapper.fromDto(null);

        assertNull(httpCookie);
    }
}
