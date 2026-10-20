package io.github.raulrezende09.groundstation.exception;

public class SatelliteNotFoundException extends RuntimeException {
    public SatelliteNotFoundException(int noradId) {
        super("No TLE found for NORAD ID " + noradId);
    }
}