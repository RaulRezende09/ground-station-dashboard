package io.github.raulrezende09.groundstation.config;

import jakarta.annotation.PostConstruct;
import org.orekit.data.DataContext;
import org.orekit.data.DirectoryCrawler;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class OrekitConfig {

    @PostConstruct
    void loadOrekitData() {
        File data = new File(System.getenv().getOrDefault("OREKIT_DATA","orekit-data"));
        DataContext.getDefault()
                .getDataProvidersManager()
                .addProvider(new DirectoryCrawler(data));
    }
}
