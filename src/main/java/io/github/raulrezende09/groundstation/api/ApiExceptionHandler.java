package io.github.raulrezende09.groundstation.api;

import io.github.raulrezende09.groundstation.tle.CelestrakUnavailableException;
import io.github.raulrezende09.groundstation.tle.SatelliteNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

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
}