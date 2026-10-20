package io.github.raulrezende09.groundstation.propagation;

import io.github.raulrezende09.groundstation.exception.InvalidStationException;
import io.github.raulrezende09.groundstation.tle.TleLines;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
class LookAnglesTest {

    // Mesmo TLE de tools/reference_look.py: se um mudar, o outro muda junto
    private static final TleLines ISS = new TleLines(
            "ISS (ZARYA)",
            "1 25544U 98067A   26255.51782528 -.00002182  00000-0 -11606-4 0  9990",
            "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537");

    private static final Station SAO_PAULO = new Station("SAO-PAULO", -23.5505, -46.6333, 760.0);

    // Tolerâncias: diferenças de UT1 e de modelo de Terra entre bibliotecas
    private static final double ANGLE_TOLERANCE_DEG = 0.1;
    private static final double RANGE_TOLERANCE_KM = 1.0;
    private static final double RATE_TOLERANCE_KM_S = 0.05;

    @Autowired
    private PropagationService propagationService;

    @Test
    void matchesSkyfieldRising() {
        assertMatches("2026-09-19T16:16:00Z", 246.3483, 17.0542, 1006.5761, -5.88035);
    }

    @Test
    void matchesSkyfieldAtClosestApproach() {
        assertMatches("2026-09-19T16:18:00Z", 312.6865, 36.7583, 578.6783, 0.47462);
    }

    @Test
    void matchesSkyfieldSetting() {
        assertMatches("2026-09-19T16:20:00Z", 11.2532, 15.1999, 1069.8994, 6.04167);
    }

    @Test
    void rejectsInvalidStations() {
        assertThatThrownBy(() -> new Station("X", 95.0, 0.0, 0.0))
                .isInstanceOf(InvalidStationException.class);
        assertThatThrownBy(() -> new Station("X", 0.0, 181.0, 0.0))
                .isInstanceOf(InvalidStationException.class);
        assertThatThrownBy(() -> new Station("X", Double.NaN, 0.0, 0.0))
                .isInstanceOf(InvalidStationException.class);
        assertThatThrownBy(() -> new Station(" ", 0.0, 0.0, 0.0))
                .isInstanceOf(InvalidStationException.class);
    }

    private void assertMatches(String instant, double az, double el, double rangeKm, double rateKmS) {
        LookAngles look = propagationService.lookAngles(ISS, SAO_PAULO, Instant.parse(instant));

        System.out.printf("%s az=%.4f el=%.4f range=%.4f rate=%.5f%n", instant,
                look.azimuthDeg(), look.elevationDeg(), look.rangeKm(), look.rangeRateKmS());

        assertThat(look.azimuthDeg()).isCloseTo(az, within(ANGLE_TOLERANCE_DEG));
        assertThat(look.elevationDeg()).isCloseTo(el, within(ANGLE_TOLERANCE_DEG));
        assertThat(look.rangeKm()).isCloseTo(rangeKm, within(RANGE_TOLERANCE_KM));
        assertThat(look.rangeRateKmS()).isCloseTo(rateKmS, within(RATE_TOLERANCE_KM_S));
    }
}