package io.github.raulrezende09.groundstation.propagation;

import java.time.Instant;

public record Position (double latitudeDeg, double LongitudeDeg, double altitudeKm,
                        double velocityKmS, Instant tleEpoch){
}
