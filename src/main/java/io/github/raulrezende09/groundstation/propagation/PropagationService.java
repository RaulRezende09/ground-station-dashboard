package io.github.raulrezende09.groundstation.propagation;

import io.github.raulrezende09.groundstation.tle.TleLines;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PropagationService {
    private Frame itrf;
    private OneAxisEllipsoid earth;

    public Position positionAt(TleLines tleLines, Instant instant) {
        TLE tle = new TLE(tleLines.line1(), tleLines.line2());
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);

        AbsoluteDate date = new AbsoluteDate(instant, TimeScalesFactory.getUTC());
        SpacecraftState state = propagator.propagate(date);

        initFramesIfNeeded();

        Vector3D position = state.getPVCoordinates(itrf).getPosition();
        GeodeticPoint geodetic = earth.transform(position, itrf, date);

        return new Position(
                FastMath.toDegrees(geodetic.getLatitude()),
                FastMath.toDegrees(geodetic.getLongitude()),
                geodetic.getAltitude() / 1000.0
        );

    }

    private synchronized void initFramesIfNeeded() {
        if (itrf == null) {
            itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            earth = new OneAxisEllipsoid(
                    Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING, itrf
            );
        }
    }

}
