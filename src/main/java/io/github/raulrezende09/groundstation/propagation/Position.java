package io.github.raulrezende09.groundstation.propagation;

import java.time.Instant;

public record Position (double latitudeDeg, double longitudeDeg, double altitudeKm,
                        double velocityKmS, Instant tleEpoch){
}
