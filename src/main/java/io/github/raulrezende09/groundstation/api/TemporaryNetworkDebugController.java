package io.github.raulrezende09.groundstation.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TemporaryNetworkDebugController {

    private static final List<String> HOSTS = List.of("celestrak.org", "google.com", "github.com");

    @GetMapping("/debug/network")
    public Map<String, Object> checkNetwork() {
        Map<String, Object> result = new LinkedHashMap<>();

        for (String host : HOSTS) {
            Map<String, Object> hostResult = new LinkedHashMap<>();

            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                List<String> ips = java.util.Arrays.stream(addresses)
                        .map(InetAddress::getHostAddress)
                        .toList();
                hostResult.put("dns", ips);
            } catch (Exception e) {
                hostResult.put("dnsError", e.toString());
            }

            try (Socket socket = new Socket()) {
                long start = System.currentTimeMillis();
                socket.connect(new InetSocketAddress(host, 443), 5000);
                long elapsed = System.currentTimeMillis() - start;
                hostResult.put("tcpConnect", "success in " + elapsed + "ms");
            } catch (Exception e) {
                hostResult.put("tcpConnectError", e.toString());
            }

            result.put(host, hostResult);
        }

        return result;
    }
}