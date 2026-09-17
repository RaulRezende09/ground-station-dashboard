package io.github.raulrezende09.groundstation.api;

import io.github.raulrezende09.groundstation.propagation.Position;
import io.github.raulrezende09.groundstation.propagation.PropagationService;
import io.github.raulrezende09.groundstation.tle.CelestrakClient;
import io.github.raulrezende09.groundstation.tle.TleLines;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/satellites")
public class SatelliteController {

    private final CelestrakClient celestrakClient;
    private final PropagationService propagationService;

    public SatelliteController(CelestrakClient celestrakClient, PropagationService propagationService) {
        this.celestrakClient = celestrakClient;
        this.propagationService = propagationService;
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
                position.LongitudeDeg(),
                position.altitudeKm(),
                position.velocityKmS(),
                position.tleEpoch()
        );
    }
}