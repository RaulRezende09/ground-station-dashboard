package io.github.raulrezende09.groundstation.api.dto;

public record FootprintDto(int noradId, String name, double radiusKm, double minElevationDeg, GeoJsonPolygon footprint) {
}