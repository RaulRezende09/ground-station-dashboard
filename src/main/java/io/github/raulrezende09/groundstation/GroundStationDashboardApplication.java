package io.github.raulrezende09.groundstation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class GroundStationDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroundStationDashboardApplication.class, args);
    }

}
