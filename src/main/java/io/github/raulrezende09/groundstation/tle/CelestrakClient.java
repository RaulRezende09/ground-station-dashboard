package io.github.raulrezende09.groundstation.tle;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CelestrakClient {

    private static final String URL = "https://celestrak.org/NORAD/elements/gp.php?CATNR={id}&FORMAT=TLE";

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
            throw new SatelliteNotFoundException(noradId);
        } catch (RestClientException e) {
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
