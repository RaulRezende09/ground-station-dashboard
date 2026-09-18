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

    private static final String MIRROR_URL =
            "https://raw.githubusercontent.com/RaulRezende09/ground-station-dashboard/main/data/tle-mirror.txt";

    private final RestClient http = RestClient.builder()
            .defaultHeader("User-Agent",
                    "ground-station-dashboard/0.1 (github.com/RaulRezende09/ground-station-dashboard)")
            .build();

    @Cacheable("gp")
    public TleLines fetch(int noradId) {
        String body;
        try {
            body = http.get().uri(MIRROR_URL).retrieve().body(String.class);
        } catch (HttpClientErrorException e) {
            log.warn("TLE mirror returned a client error: {}", e.getMessage());
            throw new SatelliteNotFoundException(noradId);
        } catch (RestClientException e) {
            log.error("Failed to reach the TLE mirror on GitHub", e);
            throw new CelestrakUnavailableException("TLE mirror is currently unavailable", e);
        }

        if (body == null || body.isBlank()) {
            throw new SatelliteNotFoundException(noradId);
        }

        String[] lines = body.strip().split("\\R");

        for (int i = 0; i + 2 < lines.length; i += 3) {
            String name = lines[i].strip();
            String line1 = lines[i + 1];
            String line2 = lines[i + 2];

            int catalogNumber = Integer.parseInt(line1.substring(2, 7).trim());
            if (catalogNumber == noradId) {
                return new TleLines(name, line1, line2);
            }
        }

        throw new SatelliteNotFoundException(noradId);
    }
}