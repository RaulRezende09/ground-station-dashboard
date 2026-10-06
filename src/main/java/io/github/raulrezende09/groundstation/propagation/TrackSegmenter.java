package io.github.raulrezende09.groundstation.propagation;

import java.util.ArrayList;
import java.util.List;

public final class TrackSegmenter {

    private TrackSegmenter() {
    }

    public static List<List<TrackPoint>> splitAtAntimeridian(List<TrackPoint> points) {
        List<List<TrackPoint>> segments = new ArrayList<>();
        List<TrackPoint> current = new ArrayList<>();

        for (TrackPoint point : points) {
            if (!current.isEmpty()) {
                TrackPoint previous = current.get(current.size() - 1);
                double previousLon = previous.longitudeDeg();
                double jump = Math.abs(point.longitudeDeg() - previousLon);

                if (jump > 180.0) {
                    // Eastward crossing: 179.8 -> -179.6. Westward: -179.8 -> 179.6.
                    double edge = previousLon > 0 ? 180.0 : -180.0;
                    double unwrappedLon = previousLon > 0
                            ? point.longitudeDeg() + 360.0
                            : point.longitudeDeg() - 360.0;

                    // How far (0..1) between the two samples the track reaches the edge.
                    double fraction = (edge - previousLon) / (unwrappedLon - previousLon);
                    double crossingLat = previous.latitudeDeg()
                            + fraction * (point.latitudeDeg() - previous.latitudeDeg());

                    current.add(new TrackPoint(crossingLat, edge));
                    segments.add(current);

                    current = new ArrayList<>();
                    current.add(new TrackPoint(crossingLat, -edge));
                }
            }
            current.add(point);
        }
        segments.add(current);

        return segments;
    }
}