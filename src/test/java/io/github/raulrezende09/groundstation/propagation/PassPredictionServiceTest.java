package io.github.raulrezende09.groundstation.propagation;

import io.github.raulrezende09.groundstation.exception.InvalidPassRequestException;
import io.github.raulrezende09.groundstation.tle.TleLines;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
class PassPredictionServiceTest {

    // Same TLE as tools/reference_passes.py
    private static final TleLines ISS = new TleLines(
            "ISS (ZARYA)",
            "1 25544U 98067A   26255.51782528 -.00002182  00000-0 -11606-4 0  9990",
            "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537");

    private static final Station SAO_PAULO = new Station("SAO-PAULO", -23.5505, -46.6333, 760.0);

    private static final Instant START = Instant.parse("2026-09-19T00:00:00Z");
    private static final Instant END = Instant.parse("2026-09-21T00:00:00Z");

    private static final double TIME_TOLERANCE_SECONDS = 2.0;
    private static final double ELEVATION_TOLERANCE_DEG = 0.1;

    private record Expected(String aos, String los, double maxElevationDeg) {
    }

    private static final List<Expected> SKYFIELD = List.of(
            new Expected("2026-09-19T06:34:19.476794Z", "2026-09-19T06:40:09.792294Z", 65.189),
            new Expected("2026-09-19T16:15:05.605147Z", "2026-09-19T16:20:41.569086Z", 36.878),
            new Expected("2026-09-20T05:27:07.287520Z", "2026-09-20T05:30:14.161300Z", 13.491),
            new Expected("2026-09-20T07:01:30.606912Z", "2026-09-20T07:06:08.315967Z", 20.415),
            new Expected("2026-09-20T15:06:22.669916Z", "2026-09-20T15:12:03.184263Z", 38.470));

    @Autowired
    private PassPredictionService service;

    @Test
    void matchesSkyfieldOver48Hours() {
        List<Pass> passes = service.predict(ISS, SAO_PAULO, START, END, 10.0);

        assertThat(passes).hasSize(SKYFIELD.size());

        for (int i = 0; i < passes.size(); i++) {
            Pass actual = passes.get(i);
            Expected expected = SKYFIELD.get(i);

            double dAos = secondsBetween(Instant.parse(expected.aos()), actual.aos());
            double dLos = secondsBetween(Instant.parse(expected.los()), actual.los());
            double dMax = Math.abs(expected.maxElevationDeg() - actual.maxElevationDeg());

            System.out.printf("pass %d: dAOS=%.3fs dLOS=%.3fs dMaxEl=%.4f deg%n", i + 1, dAos, dLos, dMax);

            assertThat(dAos).isLessThan(TIME_TOLERANCE_SECONDS);
            assertThat(dLos).isLessThan(TIME_TOLERANCE_SECONDS);
            assertThat(actual.maxElevationDeg())
                    .isCloseTo(expected.maxElevationDeg(), within(ELEVATION_TOLERANCE_DEG));
        }
    }

    @Test
    void discardsOrphanLosWhenWindowStartsMidPass() {
        // 16:18 is in the middle of the 16:15-16:20 pass: its AOS is before the window
        Instant midPass = Instant.parse("2026-09-19T16:18:00Z");

        List<Pass> passes = service.predict(ISS, SAO_PAULO, midPass, END, 10.0);

        assertThat(passes).hasSize(3);
        assertThat(passes.get(0).aos()).isAfter(midPass);
        assertThat(secondsBetween(Instant.parse("2026-09-20T05:27:07.287520Z"), passes.get(0).aos()))
                .isLessThan(TIME_TOLERANCE_SECONDS);
    }

    @Test
    void higherMinimumElevationReturnsFewerPasses() {
        List<Pass> at10 = service.predict(ISS, SAO_PAULO, START, END, 10.0);
        List<Pass> at30 = service.predict(ISS, SAO_PAULO, START, END, 30.0);

        assertThat(at30).hasSize(3);
        assertThat(at30.size()).isLessThan(at10.size());
        assertThat(at30).allSatisfy(p -> assertThat(p.maxElevationDeg()).isGreaterThanOrEqualTo(30.0));
    }

    @Test
    void trackIsConsistentWithPassBoundaries() {
        List<Pass> passes = service.predict(ISS, SAO_PAULO, START, END, 10.0);

        for (Pass pass : passes) {
            List<SkyPoint> track = pass.track();
            assertThat(track.get(0).time()).isEqualTo(pass.aos());
            assertThat(track.get(track.size() - 1).time()).isEqualTo(pass.los());
            assertThat(track.get(0).elevationDeg()).isCloseTo(10.0, within(0.2));
            assertThat(pass.maxElevationTime()).isBetween(pass.aos(), pass.los());
        }
    }

    @Test
    void rejectsInvalidRequests() {
        assertThatThrownBy(() -> service.predict(ISS, SAO_PAULO, START, END, 95.0))
                .isInstanceOf(InvalidPassRequestException.class);
        assertThatThrownBy(() -> service.predict(ISS, SAO_PAULO, END, START, 10.0))
                .isInstanceOf(InvalidPassRequestException.class);
        assertThatThrownBy(() -> service.predict(ISS, SAO_PAULO, START, START.plus(Duration.ofHours(100)), 10.0))
                .isInstanceOf(InvalidPassRequestException.class);
    }

    private static double secondsBetween(Instant a, Instant b) {
        return Math.abs(Duration.between(a, b).toMillis()) / 1000.0;
    }
}