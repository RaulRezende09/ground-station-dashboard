package io.github.raulrezende09.groundstation.api.dto;

import io.github.raulrezende09.groundstation.propagation.Station;

import java.time.Instant;

public record LookAnglesDto(
        int noradId,
        String name,
        Instant timestamp,
        Station station,
        double azimuthDeg,
        double elevationDeg,
        double rangeKm,
        double rangeRateKmS,
        boolean aboveHorizon,
        Instant tleEpoch
) {
}