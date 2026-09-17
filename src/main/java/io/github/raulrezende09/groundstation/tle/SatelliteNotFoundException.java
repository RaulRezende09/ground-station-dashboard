package io.github.raulrezende09.groundstation.tle;

public class SatelliteNotFoundException extends RuntimeException {
    public SatelliteNotFoundException(int noradId) {
        super("No TLE found for NORAD ID " + noradId);
    }
}