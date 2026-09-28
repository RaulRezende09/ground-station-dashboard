package io.github.raulrezende09.groundstation.api;

import io.github.raulrezende09.groundstation.propagation.TrackPoint;

import java.util.List;

public record GeoJsonMultiLineString(String type, List<List<List<Double>>> coordinates) {

    public static GeoJsonMultiLineString of(List<List<TrackPoint>> segments) {
        List<List<List<Double>>> coordinates = segments.stream()
                .map(segment -> segment.stream()
                        .map(point -> List.of(point.longitudeDeg(), point.latitudeDeg()))
                        .toList())
                .toList();

        return new GeoJsonMultiLineString("MultiLineString", coordinates);
    }
}