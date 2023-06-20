package pt.zerodseis.services.webscraper.mappers;

import org.mapstruct.Mapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Mapper
public interface HttpStatusMapper {

    default HttpStatusCode fromHttpStatus(HttpStatus httpStatus) {
        return httpStatus;
    }

    default HttpStatus toHttpStatus(HttpStatusCode httpStatusCode) {
        return (HttpStatus) httpStatusCode;
    }
}
