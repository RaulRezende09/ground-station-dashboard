package io.github.raulrezende09.groundstation.propagation;

import io.github.raulrezende09.groundstation.exception.InvalidStationException;

public record Station (String name, double latitudeDeg, double longitudeDeg, double altitudeM) {

    public Station {
        if(name == null || name.isBlank()){
            throw new InvalidStationException("Station name must not be blank");
        }
        if(!(latitudeDeg >= -90.0 && latitudeDeg <= 90.0)){
            throw new InvalidStationException("Latitude must be between -90 and 90 degrees");
        }
        if(!(longitudeDeg >= -180.0 && longitudeDeg <= 180.0)){
            throw new InvalidStationException("Longitude must be between -180 and 180 degrees");
        }
        if(!(altitudeM >= -500.0 && altitudeM <= 9000.0)){
            throw new InvalidStationException("Altitude must be between -500 and 9000 meters");
        }
    }
}
