package pt.zerodseis.services.webscraper.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.web.models.WebScraperResponseDto;

@Mapper(uses = {WebScraperRequestDtoMapper.class, HttpCookieDtoMapper.class,
        HttpStatusMapper.class})
public interface WebScraperResponseDtoMapper {

    @Mapping(source = "request.id", target = "requestId")
    WebScraperResponseDto toDto(WebScraperResponse response);

    @Mapping(source = "requestId", target = "request.id")
    WebScraperResponse fromDto(WebScraperResponseDto dto);

}
