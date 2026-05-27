package dk.rutesplanen.backend.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxy-controller der videresender kortanmodninger til OpenRouteService.
 * API-nøglen holdes i backend og eksponeres aldrig til klienten.
 */
@RestController
@RequestMapping("/api/map")
public class MapProxyController {

    // API-nøglen hentes fra application.properties og eksponeres aldrig til browseren
    @Value("${ors.api.key}")
    private String orsApiKey;

    // Lageradressen konfigureres via miljøvariabel i docker-compose.yml
    @Value("${warehouse.address}")
    private String warehouseAddress;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of("warehouseAddress", warehouseAddress);
    }

    // Konverterer en adresse til GPS-koordinater via OpenRouteService geocoding
    @GetMapping("/geocode")
    public ResponseEntity<String> geocode(@RequestParam String address) throws Exception {
        String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://api.openrouteservice.org/geocode/search?api_key=" + orsApiKey
                + "&text=" + encoded + "&boundary.country=DK&size=1"
                + "&focus.point.lat=55.667&focus.point.lon=12.395";
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return ResponseEntity.status(res.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(res.body());
    }

    // Beregner vejrute som GeoJSON-linje der kan tegnes direkte på Leaflet-kortet
    @PostMapping("/directions")
    public ResponseEntity<String> directions(@RequestBody String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/v2/directions/driving-car/geojson"))
                .header("Content-Type", "application/json")
                .header("Authorization", orsApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return ResponseEntity.status(res.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(res.body());
    }

    // Beregner den optimale rækkefølge for alle stop (travelling salesman via ORS)
    @PostMapping("/optimize")
    public ResponseEntity<String> optimize(@RequestBody String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/optimization"))
                .header("Content-Type", "application/json")
                .header("Authorization", orsApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return ResponseEntity.status(res.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(res.body());
    }
}
