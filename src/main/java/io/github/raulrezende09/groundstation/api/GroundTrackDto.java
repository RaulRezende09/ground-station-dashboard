package io.github.raulrezende09.groundstation.api;

import io.github.raulrezende09.groundstation.propagation.TrackPoint;

import java.util.List;

public record GroundTrackDto(int noradId, String name, List<TrackPoint> points) {
}