package pt.zerodseis.services.webscraper.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.zerodseis.services.webscraper.service.WebScraperService;
import pt.zerodseis.services.webscraper.dto.WebScraperRequestDto;
import pt.zerodseis.services.webscraper.dto.WebScraperResponseDto;

@Validated
@RequestMapping("/api/v1/scraper/")
@RestController
public class WebScraperController {

    private final WebScraperService service;

    public WebScraperController(WebScraperService service) {
        this.service = service;
    }

    @PostMapping(value = "site",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebScraperResponseDto> scrapSite(
            @Valid @RequestBody WebScraperRequestDto request) {
        return new ResponseEntity<>(service.scrapSite(request), HttpStatus.OK);
    }

    @PostMapping(value = "sites",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<WebScraperResponseDto>> scrapSites(
            @RequestBody @NotEmpty List<@Valid WebScraperRequestDto> requests) {
        return new ResponseEntity<>(service.scrapSites(requests), HttpStatus.OK);
    }
}
