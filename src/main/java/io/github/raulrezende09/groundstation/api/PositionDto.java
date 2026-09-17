package io.github.raulrezende09.groundstation.api;

import java.time.Instant;

public record PositionDto(
        int noradId,
        String name,
        Instant timestamp,
        double latitude,
        double longitude,
        double altitudeKm,
        double velocityKmS,
        Instant tleEpoch
) {
}