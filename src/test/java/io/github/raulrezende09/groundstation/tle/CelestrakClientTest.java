package io.github.raulrezende09.groundstation.tle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CelestrakClientTest {

    @Autowired
    private CelestrakClient client;

    @Test
    void fetchesIssTle() {
        TleLines tle = client.fetch(25544); // ISS (ZARYA)

        assertThat(tle.name()).contains("ISS");
        assertThat(tle.line1()).startsWith("1 25544");
        assertThat(tle.line2()).startsWith("2 25544");

        System.out.println("Fetched: " + tle);
    }
}