package tn.esprit.soa.rest.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.soa.rest.client.DisponibiliteSoapClient;
import tn.esprit.soa.rest.client.GeoClient;
import tn.esprit.soa.rest.client.MedicamentClient;
import tn.esprit.soa.rest.client.MeteoClient;
import tn.esprit.soa.rest.client.PharmacieClient;

import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class RechercheController {

    private final MedicamentClient medicaments;
    private final GeoClient geo;
    private final PharmacieClient pharmacies;
    private final MeteoClient meteo;
    private final DisponibiliteSoapClient soap;

    public RechercheController(MedicamentClient medicaments, GeoClient geo, PharmacieClient pharmacies,
                               MeteoClient meteo, DisponibiliteSoapClient soap) {
        this.medicaments = medicaments;
        this.geo = geo;
        this.pharmacies = pharmacies;
        this.meteo = meteo;
        this.soap = soap;
    }

    /** Endpoint principal : orchestre les 5 services. */
    @GetMapping("/recherche")
    public Map<String, Object> recherche(@RequestParam String medicament,
                                         @RequestParam String ville,
                                         @RequestParam(defaultValue = "3000") int rayon) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("medicament", medicament);
        out.put("ville", ville);

        // 1. Infos medicament (API Medicaments FR)
        out.put("infosMedicament", securise(() -> medicaments.rechercher(medicament)));

        // 2. Localisation (Nominatim / OpenStreetMap)
        Map<String, Object> position;
        try {
            position = geo.geocoder(ville);
        } catch (Exception e) {
            out.put("erreur", "Geocodage impossible : " + e.getMessage());
            return out;
        }
        double lat = Double.parseDouble(String.valueOf(position.get("lat")));
        double lon = Double.parseDouble(String.valueOf(position.get("lon")));
        out.put("position", Map.of("latitude", lat, "longitude", lon, "nom", position.get("display_name")));

        // 3. Meteo
        out.put("meteo", securise(() -> meteo.meteo(lat, lon)));

        // 4. Pharmacies proches (Overpass / OpenStreetMap) + 5. stock via SOAP
        List<Map<String, Object>> resultat = new ArrayList<>();
        try {
            for (Map<String, Object> p : pharmacies.autour(lat, lon, rayon, 5)) {
                Map<String, Object> ligne = new LinkedHashMap<>(p);
                ligne.put("stock", securise(() -> soap.verifier(medicament, String.valueOf(p.get("id")))));
                resultat.add(ligne);
            }
        } catch (Exception e) {
            out.put("erreurPharmacies", e.getMessage());
        }
        out.put("pharmacies", resultat);
        return out;
    }

    // ---- Endpoints unitaires, pratiques pour tester chaque service ----

    @GetMapping("/medicaments")
    public Object medicaments(@RequestParam String nom) {
        return medicaments.rechercher(nom);
    }

    @GetMapping("/pharmacies")
    public List<Map<String, Object>> pharmacies(@RequestParam String ville,
                                                @RequestParam(defaultValue = "3000") int rayon) {
        Map<String, Object> p = geo.geocoder(ville);
        return pharmacies.autour(Double.parseDouble(String.valueOf(p.get("lat"))),
                Double.parseDouble(String.valueOf(p.get("lon"))), rayon, 10);
    }

    @GetMapping("/meteo")
    public Map<String, Object> meteo(@RequestParam String ville) {
        Map<String, Object> p = geo.geocoder(ville);
        return meteo.meteo(Double.parseDouble(String.valueOf(p.get("lat"))),
                Double.parseDouble(String.valueOf(p.get("lon"))));
    }

    @GetMapping("/disponibilite")
    public Map<String, Object> disponibilite(@RequestParam String medicament,
                                             @RequestParam(defaultValue = "pharmacie-1") String pharmacieId) {
        try {
            return soap.verifier(medicament, pharmacieId);
        } catch (Exception e) {
            return Map.of("erreur", String.valueOf(e.getMessage()));
        }
    }

    /** Un service externe qui tombe ne doit pas casser toute la reponse. */
    private Object securise(Supplier<Object> appel) {
        try {
            return appel.get();
        } catch (Exception e) {
            return Map.of("erreur", String.valueOf(e.getMessage()));
        }
    }
}
