package pt.zerodseis.services.webscraper.exceptions.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pt.zerodseis.services.webscraper.exceptions.ConnectionProviderRuntimeException;
import pt.zerodseis.services.webscraper.exceptions.ReadUserAgentJsonException;
import pt.zerodseis.services.webscraper.dto.ApiError;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        String errorMsg;
        if (ex.getLocalizedMessage().startsWith("Required request body is missing")) {
            errorMsg = "Required request body is missing";
        } else {
            errorMsg = ex.getLocalizedMessage();
        }

        ApiError apiError = buildBadRequestDataError()
                .errors(List.of(errorMsg))
                .build();

        LOG.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<String> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(String.format("%s : %s", error.getField(), error.getDefaultMessage()));
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(String.format("%s : %s", error.getObjectName(), error.getDefaultMessage()));
        }

        ApiError apiError = buildBadRequestDataError()
                .errors(errors)
                .build();

        LOG.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException ex) {

        List<String> errors = new ArrayList<>();

        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            errors.add(String.format("%s : %s", cv.getPropertyPath(), cv.getMessage()));
        }

        ApiError apiError = buildBadRequestDataError()
                .errors(errors)
                .build();

        LOG.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConnectionProviderRuntimeException.class)
    public ResponseEntity<ApiError> handleConnectionProviderRuntimeException(ConnectionProviderRuntimeException ex) {
        ApiError apiError = buildInternalServerError()
                .message("Web Connection Provider Runtime Error")
                .errors(Collections.emptyList())
                .build();

        LOG.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(ReadUserAgentJsonException.class)
    public ResponseEntity<ApiError> handleUserAgentJsonException(ReadUserAgentJsonException ex) {
        ApiError apiError = buildInternalServerError()
                .message("Default User Agents Load Error")
                .errors(Collections.emptyList())
                .build();

        LOG.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleFallbackException(Exception ex) {
        ApiError apiError = buildInternalServerError()
                .message("Unexpected Exception")
                .errors(Collections.emptyList())
                .build();

        LOG.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private ApiError.ApiErrorBuilder buildBadRequestDataError() {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .message("Bad Request Data");
    }

    private ApiError.ApiErrorBuilder buildInternalServerError() {
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(LocalDateTime.now());
    }
}
