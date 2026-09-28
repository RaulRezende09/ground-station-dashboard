package io.github.raulrezende09.groundstation.api.dto;

import io.github.raulrezende09.groundstation.propagation.TrackPoint;

import java.util.List;

public record GeoJsonPolygon(String type, List<List<List<Double>>> coordinates) {

    public static GeoJsonPolygon of(List<TrackPoint> ring) {
        List<List<Double>> positions = ring.stream()
                .map(point -> List.of(point.longitudeDeg(), point.latitudeDeg()))
                .toList();

        return new GeoJsonPolygon("Polygon", List.of(positions));
    }
}