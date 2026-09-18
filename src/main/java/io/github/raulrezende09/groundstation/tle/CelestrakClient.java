package io.github.raulrezende09.groundstation.tle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CelestrakClient {

    private static final Logger log = LoggerFactory.getLogger(CelestrakClient.class);

    private static final String URL =
            "https://celestrak.org/NORAD/elements/gp.php?CATNR={id}&FORMAT=TLE";

    private final RestClient http = RestClient.builder()
            .defaultHeader("User-Agent",
                    "ground-station-dashboard/0.1 (github.com/RaulRezende09/ground-station-dashboard)")
            .build();

    @Cacheable("gp")
    public TleLines fetch(int noradId) {
        String body;
        try {
            body = http.get().uri(URL, noradId).retrieve().body(String.class);
        } catch (HttpClientErrorException e) {
            log.warn("Celestrak returned a client error for NORAD ID {}: {}", noradId, e.getMessage());
            throw new SatelliteNotFoundException(noradId);
        } catch (RestClientException e) {
            log.error("Failed to reach Celestrak for NORAD ID {}", noradId, e);
            throw new CelestrakUnavailableException("Celestrak is currently unavailable", e);
        }

        if (body == null || body.isBlank()) {
            throw new SatelliteNotFoundException(noradId);
        }

        String[] lines = body.strip().split("\\R");
        if (lines.length < 3) {
            throw new SatelliteNotFoundException(noradId);
        }

        return new TleLines(lines[0].strip(), lines[1], lines[2]);
    }
}