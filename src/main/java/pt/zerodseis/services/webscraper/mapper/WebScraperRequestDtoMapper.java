package pt.zerodseis.services.webscraper.mapper;

import org.mapstruct.Mapper;
import pt.zerodseis.services.webscraper.connections.WebScraperRequest;
import pt.zerodseis.services.webscraper.dto.WebScraperRequestDto;

@Mapper(uses = HttpCookieDtoMapper.class)
public interface WebScraperRequestDtoMapper {

    WebScraperRequestDto toDto(WebScraperRequest response);

    WebScraperRequest fromDto(WebScraperRequestDto dto);
}
