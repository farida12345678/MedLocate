package tn.esprit.soa.rest.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** API Medicaments FR (BDPM) : https://medicaments-api.giygas.dev */
@Component
public class MedicamentClient {

    private final RestClient http = RestClient.builder()
            .baseUrl("https://medicaments-api.giygas.dev")
            .build();

    public Object rechercher(String nom) {
        Object res = http.get()
                .uri(u -> u.path("/v1/medicaments")
                        .queryParam("search", nom)
                        .build())
                .retrieve()
                .body(Object.class);

        // L'API peut renvoyer beaucoup de résultats : on n'en garde que 3
        if (res instanceof java.util.List<?> liste && liste.size() > 3) {
            return liste.subList(0, 3);
        }
        return res;
    }
}
