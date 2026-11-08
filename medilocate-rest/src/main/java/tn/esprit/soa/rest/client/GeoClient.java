package tn.esprit.soa.rest.client;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Nominatim (OpenStreetMap) : convertit un nom de ville en coordonnees. */
@Component
public class GeoClient {

    private final RestClient http = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            // Nominatim exige un User-Agent identifiable
            .defaultHeader("User-Agent", "MediLocate-ESPRIT-SOA/1.0")
            .build();

    @SuppressWarnings("unchecked")
    public Map<String, Object> geocoder(String ville) {
        List<Map<String, Object>> res = http.get()
                .uri(u -> u.path("/search")
                        .queryParam("q", ville)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(List.class);
        if (res == null || res.isEmpty()) {
            throw new IllegalArgumentException("Ville introuvable : " + ville);
        }
        return res.get(0);
    }
}
