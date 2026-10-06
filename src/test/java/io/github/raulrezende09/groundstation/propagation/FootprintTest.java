package io.github.raulrezende09.groundstation.propagation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class FootprintTest {

    @Test
    void ordinaryCircleIsClosedAndHas73Points() {
        List<TrackPoint> ring = Footprint.ring(-15.7, 156.9, 427.0, 10.0);

        assertThat(ring).hasSize(73);
        assertThat(ring.get(72)).isEqualTo(ring.get(0));
    }

    @Test
    void circleAcrossTheAntimeridianKeepsLongitudeContinuous() {
        List<TrackPoint> ring = Footprint.ring(20.0, 178.0, 420.0, 10.0);

        assertThat(ring).hasSize(73);
        for (int i = 1; i < ring.size(); i++) {
            double jump = Math.abs(ring.get(i).longitudeDeg() - ring.get(i - 1).longitudeDeg());
            assertThat(jump).isLessThan(90.0);
        }
    }

    @Test
    void circleEnclosingTheNorthPoleIsClosedThroughThePole() {
        List<TrackPoint> ring = Footprint.ring(75.3, -3.0, 849.0, 10.0);

        assertThat(ring).hasSize(76);
        assertThat(ring.get(73).latitudeDeg()).isEqualTo(90.0);
        assertThat(ring.get(74).latitudeDeg()).isEqualTo(90.0);
        assertThat(ring.get(75)).isEqualTo(ring.get(0));

        double sweep = Math.abs(ring.get(72).longitudeDeg() - ring.get(0).longitudeDeg());
        assertThat(sweep).isCloseTo(360.0, within(1e-6));
    }

    @Test
    void circleEnclosingTheSouthPoleIsClosedThroughThePole() {
        List<TrackPoint> ring = Footprint.ring(-80.0, 40.0, 849.0, 10.0);

        assertThat(ring).hasSize(76);
        assertThat(ring.get(73).latitudeDeg()).isEqualTo(-90.0);
        assertThat(ring.get(74).latitudeDeg()).isEqualTo(-90.0);
        assertThat(ring.get(75)).isEqualTo(ring.get(0));
    }

    @Test
    void firstVertexLongitudeStaysWithinOneWorld() {
        List<TrackPoint> ring = Footprint.ring(-78.0, -170.0, 849.0, 10.0);

        assertThat(ring.get(0).longitudeDeg()).isBetween(-180.0, 180.0);
    }
}