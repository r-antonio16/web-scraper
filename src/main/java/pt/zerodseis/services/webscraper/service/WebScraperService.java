package pt.zerodseis.services.webscraper.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import pt.zerodseis.services.webscraper.connections.WebScraperResponse;
import pt.zerodseis.services.webscraper.mappers.WebScraperRequestDtoMapper;
import pt.zerodseis.services.webscraper.mappers.WebScraperResponseDtoMapper;
import pt.zerodseis.services.webscraper.runners.WebScraperRunner;
import pt.zerodseis.services.webscraper.web.models.WebScraperRequestDto;
import pt.zerodseis.services.webscraper.web.models.WebScraperResponseDto;

@Service
public class WebScraperService {

    private final WebScraperRunner runner;
    private final WebScraperRequestDtoMapper webScraperRequestDtoMapper;
    private final WebScraperResponseDtoMapper webScraperResponseDtoMapper;

    public WebScraperService(WebScraperRunner runner,
            WebScraperRequestDtoMapper webScraperRequestDtoMapper,
            WebScraperResponseDtoMapper webScraperResponseDtoMapper) {
        this.runner = runner;
        this.webScraperRequestDtoMapper = webScraperRequestDtoMapper;
        this.webScraperResponseDtoMapper = webScraperResponseDtoMapper;
    }

    public WebScraperResponseDto scrapSite(WebScraperRequestDto dto) {
        WebScraperResponse response = runner.scrapSite(webScraperRequestDtoMapper.fromDto(dto));
        return webScraperResponseDtoMapper.toDto(response);
    }

    public List<WebScraperResponseDto> scrapSites(List<WebScraperRequestDto> dto) {
        return Optional.ofNullable(dto)
                .map(list -> list.stream()
                        .map(webScraperRequestDtoMapper::fromDto)
                        .collect(Collectors.toList()))
                .map(runner::scrapSites)
                .map(responses -> responses.stream()
                        .map(webScraperResponseDtoMapper::toDto)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
