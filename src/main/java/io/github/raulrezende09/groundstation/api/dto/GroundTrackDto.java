package io.github.raulrezende09.groundstation.api.dto;

public record GroundTrackDto(int noradId, String name, GeoJsonMultiLineString track) {
}