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

        // Start the sweep towards the nearest pole, so that when the circle
        // encloses that pole, the first vertex is the one just "beyond" it.
        double startBearing = centerLatDeg >= 0 ? 0.0 : FastMath.PI;

        List<TrackPoint> points = new ArrayList<>();
        double previousLonDeg = 0.0;
        for (int i = 0; i <= VERTEX_COUNT; i++) {
            double bearing = startBearing + 2 * FastMath.PI * i / VERTEX_COUNT;

            double lat2 = FastMath.asin(
                    FastMath.sin(lat1) * FastMath.cos(rho)
                            + FastMath.cos(lat1) * FastMath.sin(rho) * FastMath.cos(bearing)
            );
            double lon2 = lon1 + FastMath.atan2(
                    FastMath.sin(bearing) * FastMath.sin(rho) * FastMath.cos(lat1),
                    FastMath.cos(rho) - FastMath.sin(lat1) * FastMath.sin(lat2)
            );

            // Keep the longitude continuous with the previous vertex.
            double lonDeg = FastMath.toDegrees(lon2);
            if (i > 0) {
                lonDeg = previousLonDeg + normalizeDeltaDeg(lonDeg - previousLonDeg);
            }
            previousLonDeg = lonDeg;

            points.add(new TrackPoint(FastMath.toDegrees(lat2), lonDeg));
        }

        return closeRing(points, centerLatDeg);
    }

    // Brings an angle difference into [-180, 180]. Example: 358 becomes -2.
    private static double normalizeDeltaDeg(double deltaDeg) {
        return deltaDeg - 360.0 * Math.rint(deltaDeg / 360.0);
    }

    private static List<TrackPoint> closeRing(List<TrackPoint> points, double centerLatDeg) {
        double firstLon = points.get(0).longitudeDeg();
        double lastLon = points.get(VERTEX_COUNT).longitudeDeg();

        // After unwrapping, a circle around a pole ends ~360 degrees away from where it started.
        boolean enclosesPole = Math.abs(lastLon - firstLon) > 180.0;

        // Shift everything so the first vertex sits in [-180, 180].
        double shift = -360.0 * Math.rint(firstLon / 360.0);
        List<TrackPoint> ring = new ArrayList<>();
        for (TrackPoint point : points) {
            ring.add(new TrackPoint(point.latitudeDeg(), point.longitudeDeg() + shift));
        }

        if (enclosesPole) {
            double poleLat = centerLatDeg >= 0 ? 90.0 : -90.0;
            ring.add(new TrackPoint(poleLat, ring.get(VERTEX_COUNT).longitudeDeg()));
            ring.add(new TrackPoint(poleLat, ring.get(0).longitudeDeg()));
            ring.add(ring.get(0));
        } else {
            // Make the ring exactly closed (the last vertex was only numerically equal).
            ring.set(VERTEX_COUNT, ring.get(0));
        }

        return ring;
    }
}