package io.github.raulrezende09.groundstation.propagation;

import io.github.raulrezende09.groundstation.exception.InvalidPassRequestException;
import io.github.raulrezende09.groundstation.tle.TleLines;
import org.hipparchus.util.FastMath;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.EventsLogger;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PassPredictionService {

    private static final double MAX_CHECK_SECONDS = 30.0;
    private static final int TRACK_STEP_SECONDS = 10;
    private static final long MAX_WINDOW_HOURS = 72;

    private final PropagationService propagationService;

    public PassPredictionService(PropagationService propagationService) {
        this.propagationService = propagationService;
    }

    public List<Pass> predict(TleLines tleLines, Station station,
                              Instant from, Instant to, double minElevationDeg) {
        validate(from, to, minElevationDeg);

        TLEPropagator propagator = propagationService.propagatorFor(tleLines);
        TopocentricFrame topo = propagationService.topocentricFrame(station);

        ElevationDetector detector = new ElevationDetector(topo)
                .withConstantElevation(FastMath.toRadians(minElevationDeg))
                .withMaxCheck(MAX_CHECK_SECONDS)
                .withHandler(new ContinueOnEvent());

        EventsLogger logger = new EventsLogger();
        propagator.addEventDetector(logger.monitorDetector(detector));

        propagator.propagate(
                new AbsoluteDate(from, TimeScalesFactory.getUTC()),
                new AbsoluteDate(to, TimeScalesFactory.getUTC()));

        List<Pass> passes = new ArrayList<>();
        Instant aos = null;
        for (EventsLogger.LoggedEvent event : logger.getLoggedEvents()) {
            Instant when = event.getDate().toDate(TimeScalesFactory.getUTC()).toInstant();
            if (event.isIncreasing()) {
                aos = when;
            } else if (aos != null) {
                passes.add(buildPass(tleLines, station, aos, when));
                aos = null;
            }
            // decreasing with no pending AOS = orphan LOS (window started mid-pass): ignored
        }
        return passes;
    }

    private void validate(Instant from, Instant to, double minElevationDeg) {
        if (!(minElevationDeg >= 0.0 && minElevationDeg < 90.0)) {
            throw new InvalidPassRequestException("Minimum elevation must be between 0 and 90 degrees");
        }
        Duration window = Duration.between(from, to);
        if (window.isNegative() || window.isZero()) {
            throw new InvalidPassRequestException("Window end must be after window start");
        }
        if (window.toHours() > MAX_WINDOW_HOURS) {
            throw new InvalidPassRequestException("Window must not exceed " + MAX_WINDOW_HOURS + " hours");
        }
    }

    private Pass buildPass(TleLines tleLines, Station station, Instant aos, Instant los) {
        List<SkyPoint> track = new ArrayList<>();
        for (Instant t = aos; t.isBefore(los); t = t.plusSeconds(TRACK_STEP_SECONDS)) {
            track.add(sample(tleLines, station, t));
        }
        track.add(sample(tleLines, station, los));

        SkyPoint peak = refinedPeak(tleLines, station, track, aos, los);

        double durationSeconds = Duration.between(aos, los).toMillis() / 1000.0;
        return new Pass(
                aos,
                los,
                durationSeconds,
                peak.elevationDeg(),
                peak.time(),
                track.get(0).azimuthDeg(),
                track.get(track.size() - 1).azimuthDeg(),
                track);
    }

    private SkyPoint sample(TleLines tleLines, Station station, Instant t) {
        LookAngles look = propagationService.lookAngles(tleLines, station, t);
        return new SkyPoint(t, look.azimuthDeg(), look.elevationDeg());
    }

    private SkyPoint refinedPeak(TleLines tleLines, Station station,
                                 List<SkyPoint> track, Instant aos, Instant los) {
        SkyPoint best = track.get(0);
        for (SkyPoint p : track) {
            if (p.elevationDeg() > best.elevationDeg()) {
                best = p;
            }
        }

        Instant from = best.time().minusSeconds(TRACK_STEP_SECONDS);
        Instant to = best.time().plusSeconds(TRACK_STEP_SECONDS);
        if (from.isBefore(aos)) {
            from = aos;
        }
        if (to.isAfter(los)) {
            to = los;
        }

        for (Instant t = from; !t.isAfter(to); t = t.plusSeconds(1)) {
            SkyPoint p = sample(tleLines, station, t);
            if (p.elevationDeg() > best.elevationDeg()) {
                best = p;
            }
        }
        return best;
    }
}
