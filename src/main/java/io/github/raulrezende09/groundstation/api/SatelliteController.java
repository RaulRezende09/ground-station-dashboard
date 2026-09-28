package io.github.raulrezende09.groundstation.api;

import io.github.raulrezende09.groundstation.api.dto.*;
import io.github.raulrezende09.groundstation.propagation.*;
import io.github.raulrezende09.groundstation.tle.CelestrakClient;
import io.github.raulrezende09.groundstation.tle.TleLines;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/satellites")
public class SatelliteController {

    private final CelestrakClient celestrakClient;
    private final PropagationService propagationService;

    public SatelliteController(CelestrakClient celestrakClient, PropagationService propagationService) {
        this.celestrakClient = celestrakClient;
        this.propagationService = propagationService;
    }

    @GetMapping("/**")
    public ResponseEntity<Map<String, String>> handleMalformedSatelliteUrl() {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Unrecognized satellite endpoint, or a NORAD ID is missing/invalid in the URL",
                "examples", "/api/satellites/25544/position, /api/satellites/25544/groundtrack, /api/satellites/25544/footprint"
        ));
    }

    @GetMapping("/{noradId}/position")
    public PositionDto position(
            @PathVariable int noradId,
            @RequestParam(required = false) Instant at) {

        Instant instant = at != null ? at : Instant.now();
        TleLines tle = celestrakClient.fetch(noradId);
        Position position = propagationService.positionAt(tle, instant);

        return new PositionDto(
                noradId,
                tle.name(),
                instant,
                position.latitudeDeg(),
                position.longitudeDeg(),
                position.altitudeKm(),
                position.velocityKmS(),
                position.tleEpoch()
        );
    }

    @GetMapping("/{noradId}/groundtrack")
    public GroundTrackDto groundTrack(
            @PathVariable int noradId,
            @RequestParam(defaultValue = "3600") int spanSeconds,
            @RequestParam(defaultValue = "30") int stepSeconds) {

        TleLines tle = celestrakClient.fetch(noradId);
        List<TrackPoint> points = propagationService.groundTrack(tle, Instant.now(), spanSeconds, stepSeconds);
        List<List<TrackPoint>> segments = TrackSegmenter.splitAtAntimeridian(points);
        GeoJsonMultiLineString track = GeoJsonMultiLineString.of(segments);

        return new GroundTrackDto(noradId, tle.name(), track);
    }

    @GetMapping("/{noradId}/footprint")
    public FootprintDto footprint(
            @PathVariable int noradId,
            @RequestParam(defaultValue = "10") double minElevationDeg) {

        TleLines tle = celestrakClient.fetch(noradId);
        Position position = propagationService.positionAt(tle, Instant.now());

        double radiusKm = Footprint.radiusKm(position.altitudeKm(), minElevationDeg);
        List<TrackPoint> ring = Footprint.ring(
                position.latitudeDeg(), position.longitudeDeg(),
                position.altitudeKm(), minElevationDeg);
        GeoJsonPolygon footprint = GeoJsonPolygon.of(ring);

        return new FootprintDto(noradId, tle.name(), radiusKm, minElevationDeg, footprint);
    }
}