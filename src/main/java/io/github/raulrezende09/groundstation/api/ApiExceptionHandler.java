package io.github.raulrezende09.groundstation.api;

import io.github.raulrezende09.groundstation.tle.CelestrakUnavailableException;
import io.github.raulrezende09.groundstation.tle.SatelliteNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(SatelliteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(SatelliteNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(CelestrakUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleUnavailable(CelestrakUnavailableException e) {
        return Map.of("error", "Celestrak is currently unavailable");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        String detail = (ex instanceof ErrorResponse errorResponse && errorResponse.getBody().getDetail() != null)
                ? errorResponse.getBody().getDetail()
                : "The request could not be processed";

        return new ResponseEntity<>(Map.of("error", detail), headers, statusCode);
    }
}