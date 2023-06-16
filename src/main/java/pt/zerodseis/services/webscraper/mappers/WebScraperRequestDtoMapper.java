package pt.zerodseis.services.webscraper.mappers;

import org.mapstruct.Mapper;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.web.models.WebScraperRequestDto;

@Mapper(uses = HttpCookieDtoMapper.class)
public interface WebScraperRequestDtoMapper {

    WebScraperRequestDto toDto(WebScraperRequest response);

    WebScraperRequest fromDto(WebScraperRequestDto dto);
}
