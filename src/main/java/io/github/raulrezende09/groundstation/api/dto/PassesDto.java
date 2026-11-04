package io.github.raulrezende09.groundstation.api.dto;

import io.github.raulrezende09.groundstation.propagation.Pass;
import io.github.raulrezende09.groundstation.propagation.Station;

import java.time.Instant;
import java.util.List;

public record PassesDto(
        int noradId,
        String name,
        Station station,
        double minElevationDeg,
        Instant from,
        Instant to,
        Instant tleEpoch,
        List<Pass> passes
) {
}