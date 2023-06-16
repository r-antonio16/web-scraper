package pt.zerodseis.services.webscraper.web.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.zerodseis.services.webscraper.service.WebScraperService;
import pt.zerodseis.services.webscraper.web.models.WebScraperRequestDto;
import pt.zerodseis.services.webscraper.web.models.WebScraperResponseDto;

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
    public ResponseEntity<WebScraperResponseDto> scrapSite(@RequestBody WebScraperRequestDto request) {
        return new ResponseEntity<>(service.scrapSite(request), HttpStatus.OK);
    }

    @PostMapping(value = "sites",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<WebScraperResponseDto>> scrapSites(
            @RequestBody List<WebScraperRequestDto> requests) {
        return new ResponseEntity<>(service.scrapSites(requests), HttpStatus.OK);
    }
}
