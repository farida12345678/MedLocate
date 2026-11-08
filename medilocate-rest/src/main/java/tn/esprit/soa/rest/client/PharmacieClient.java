package tn.esprit.soa.rest.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Pharmacies autour d'un point (données OpenStreetMap).
 * 1) Overpass (serveurs publics, souvent saturés)  2) secours : Nominatim.
 */
@Component
public class PharmacieClient {

    private static final List<String> SERVEURS_OVERPASS = List.of(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter");

    private final RestClient http;

    public PharmacieClient() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        this.http = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "MediLocate-ESPRIT-SOA/1.0")
                .build();
    }

    public List<Map<String, Object>> autour(double lat, double lon, int rayonMetres, int max) {
        try {
            return viaOverpass(lat, lon, rayonMetres, max);
        } catch (Exception e) {
            // Overpass indisponible : on bascule sur Nominatim
            return viaNominatim(lat, lon, rayonMetres, max);
        }
    }

    // ---------- 1) Overpass ----------
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> viaOverpass(double lat, double lon, int rayon, int max) {
        String query = "[out:json][timeout:8];"
                + "node[\"amenity\"=\"pharmacy\"](around:" + rayon + "," + lat + "," + lon + ");"
                + "out " + max + ";";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("data", query);

        Exception derniere = null;
        for (String serveur : SERVEURS_OVERPASS) {
            try {
                Map<String, Object> res = http.post()
                        .uri(serveur)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(Map.class);
                List<Map<String, Object>> out = new ArrayList<>();
                if (res != null && res.get("elements") != null) {
                    for (Map<String, Object> el : (List<Map<String, Object>>) res.get("elements")) {
                        Map<String, Object> tags = (Map<String, Object>) el.getOrDefault("tags", Map.of());
                        out.add(pharmacie(String.valueOf(el.get("id")),
                                tags.getOrDefault("name", "Pharmacie (sans nom)"),
                                tags.getOrDefault("addr:street", ""),
                                tags.getOrDefault("phone", ""),
                                el.get("lat"), el.get("lon")));
                    }
                }
                return out;
            } catch (Exception e) {
                derniere = e;
            }
        }
        throw new IllegalStateException("Overpass indisponible", derniere);
    }

    // ---------- 2) Secours : Nominatim ----------
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> viaNominatim(double lat, double lon, int rayon, int max) {
        double dLat = rayon / 111_000.0;
        double dLon = rayon / (111_000.0 * Math.cos(Math.toRadians(lat)));
        // viewbox = gauche,haut,droite,bas
        String viewbox = (lon - dLon) + "," + (lat + dLat) + "," + (lon + dLon) + "," + (lat - dLat);

        List<Map<String, Object>> res = http.get()
                .uri("https://nominatim.openstreetmap.org/search?q=pharmacy&viewbox={vb}&bounded=1"
                        + "&format=jsonv2&addressdetails=1&limit={max}", viewbox, max)
                .retrieve()
                .body(List.class);

        List<Map<String, Object>> out = new ArrayList<>();
        if (res == null) return out;
        for (Map<String, Object> r : res) {
            Map<String, Object> adresse = (Map<String, Object>) r.getOrDefault("address", Map.of());
            String nom = String.valueOf(r.getOrDefault("name", ""));
            if (nom.isBlank()) nom = "Pharmacie (sans nom)";
            out.add(pharmacie(String.valueOf(r.get("osm_id")), nom,
                    adresse.getOrDefault("road", ""), "",
                    Double.valueOf(String.valueOf(r.get("lat"))),
                    Double.valueOf(String.valueOf(r.get("lon")))));
        }
        return out;
    }

    private Map<String, Object> pharmacie(String id, Object nom, Object adresse, Object tel, Object lat, Object lon) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", id);
        p.put("nom", nom);
        p.put("adresse", adresse);
        p.put("telephone", tel);
        p.put("latitude", lat);
        p.put("longitude", lon);
        return p;
    }
}