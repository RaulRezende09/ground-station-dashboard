package io.github.raulrezende09.groundstation.propagation;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.orekit.data.DataContext;
import org.orekit.data.DirectoryCrawler;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class PropagationValidationTest {

    @BeforeAll
    static void loadOrekitData() {
        File data = new File("orekit-data");
        DataContext.getDefault().getDataProvidersManager().addProvider(new DirectoryCrawler(data));
    }

    private static final String LINE_1 =
            "1 25544U 98067A   26255.51782528 -.00002182  00000-0 -11606-4 0  9990";
    private static final String LINE_2 =
            "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537";

    private static final Vector3D REFERENCE_TEME_KM =
            new Vector3D(-3120.374, -4979.400, 3279.505);

    @Test
    void matchesReferenceImplementation() {
        TLE tle = new TLE(LINE_1, LINE_2);
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);

        AbsoluteDate date = new AbsoluteDate(2026, 9, 19, 12, 0, 0, TimeScalesFactory.getUTC());
        SpacecraftState state = propagator.propagate(date);

        // Sem passar Frame nenhum: pega a posição crua em TEME, igual ao python-sgp4
        Vector3D myPositionMeters = state.getPVCoordinates().getPosition();
        Vector3D myPositionKm = new Vector3D(1.0 / 1000.0, myPositionMeters);

        double deviationKm = myPositionKm.distance(REFERENCE_TEME_KM);
        double deviationMeters = deviationKm * 1000.0;

        System.out.println("Deviation vs python-sgp4: " + deviationMeters + " m");

        assertThat(deviationMeters)
                .as("desvio contra python-sgp4")
                .isLessThan(1000.0); // meio quilômetro de folga generosa
    }
}