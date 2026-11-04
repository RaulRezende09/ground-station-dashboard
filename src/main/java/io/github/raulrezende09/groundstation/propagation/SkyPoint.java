package io.github.raulrezende09.groundstation.propagation;

import java.time.Instant;

public record SkyPoint(
        Instant time,
        double azimuthDeg,
        double elevationDeg
) {
}
