package pt.zerodseis.services.webscraper.web.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ApiError(HttpStatus status,
                       @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
                       LocalDateTime timestamp,
                       String message,
                       List<String> errors) {

}
