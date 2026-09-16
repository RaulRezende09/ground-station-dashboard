package io.github.raulrezende09.groundstation.propagation;

import io.github.raulrezende09.groundstation.tle.CelestrakClient;
import io.github.raulrezende09.groundstation.tle.TleLines;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PropagationServiceTest {

    @Autowired
    private CelestrakClient celestrakClient;

    @Autowired
    private PropagationService propagationService;

    @Test
    void propagatesIssToPlausibleCoordinates() {
        TleLines tle = celestrakClient.fetch(25544); // ISS (ZARYA)

        Position position = propagationService.positionAt(tle, Instant.now());

        System.out.println(position);

        assertThat(position.latitudeDeg()).isBetween(-90.0, 90.0);
        assertThat(position.LongitudeDeg()).isBetween(-180.0, 180.0);
        assertThat(position.altitudeKm()).isBetween(300.0, 500.0); // órbita típica da ISS
    }
}