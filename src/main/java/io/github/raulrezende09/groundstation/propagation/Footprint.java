package io.github.raulrezende09.groundstation.propagation;

import org.hipparchus.util.FastMath;
import org.orekit.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public final class Footprint {

    private static final double EARTH_RADIUS_KM = Constants.WGS84_EARTH_EQUATORIAL_RADIUS / 1000.0;
    private static final int VERTEX_COUNT = 72;

    private Footprint() {
    }

    public static double angularRadiusRad(double altitudeKm, double minElevationDeg) {
        double eps = FastMath.toRadians(minElevationDeg);
        double ratio = EARTH_RADIUS_KM * FastMath.cos(eps) / (EARTH_RADIUS_KM + altitudeKm);
        return FastMath.acos(ratio) - eps;
    }

    public static double radiusKm(double altitudeKm, double minElevationDeg) {
        return angularRadiusRad(altitudeKm, minElevationDeg) * EARTH_RADIUS_KM;
    }

    public static List<TrackPoint> ring(double centerLatDeg, double centerLonDeg,
                                        double altitudeKm, double minElevationDeg) {
        double rho = angularRadiusRad(altitudeKm, minElevationDeg);
        double lat1 = FastMath.toRadians(centerLatDeg);
        double lon1 = FastMath.toRadians(centerLonDeg);

        List<TrackPoint> points = new ArrayList<>();
        for (int i = 0; i <= VERTEX_COUNT; i++) {
            double bearing = 2 * FastMath.PI * i / VERTEX_COUNT;

            double lat2 = FastMath.asin(
                    FastMath.sin(lat1) * FastMath.cos(rho)
                            + FastMath.cos(lat1) * FastMath.sin(rho) * FastMath.cos(bearing)
            );
            double lon2 = lon1 + FastMath.atan2(
                    FastMath.sin(bearing) * FastMath.sin(rho) * FastMath.cos(lat1),
                    FastMath.cos(rho) - FastMath.sin(lat1) * FastMath.sin(lat2)
            );

            points.add(new TrackPoint(FastMath.toDegrees(lat2), FastMath.toDegrees(lon2)));
        }

        return points;
    }
}