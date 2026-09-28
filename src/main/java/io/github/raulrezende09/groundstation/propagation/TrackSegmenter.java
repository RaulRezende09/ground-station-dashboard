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
                double previousLongitude = current.get(current.size() - 1).longitudeDeg();
                double jump = Math.abs(point.longitudeDeg() - previousLongitude);
                if (jump > 180.0) {
                    segments.add(current);
                    current = new ArrayList<>();
                }
            }
            current.add(point);
        }
        segments.add(current);

        return segments;
    }
}