package io.github.raulrezende09.groundstation.propagation;

import java.time.Instant;

public record LookAngles(
        double azimuthDeg,
        double elevationDeg,
        double rangeKm,
        double rangeRateKmS,
        Instant tleEpoch) {
}
