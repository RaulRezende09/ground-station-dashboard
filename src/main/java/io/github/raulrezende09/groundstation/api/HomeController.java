package io.github.raulrezende09.groundstation.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Ground Station Dashboard");
        body.put("status", "ok");
        body.put("endpoints", Map.of(
                "position", "/api/satellites/{noradId}/position",
                "groundtrack", "/api/satellites/{noradId}/groundtrack",
                "footprint", "/api/satellites/{noradId}/footprint"
        ));
        return body;
    }
}