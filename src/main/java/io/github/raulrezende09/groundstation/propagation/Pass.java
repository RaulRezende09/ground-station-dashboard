package io.github.raulrezende09.groundstation.propagation;

import java.time.Instant;
import java.util.List;

public record Pass(
        Instant aos,
        Instant los,
        double durationSeconds,
        double maxElevationDeg,
        Instant maxElevationTime,
        double aosAzimuthDeg,
        double losAzimuthDeg,
        List<SkyPoint> track
) {
}