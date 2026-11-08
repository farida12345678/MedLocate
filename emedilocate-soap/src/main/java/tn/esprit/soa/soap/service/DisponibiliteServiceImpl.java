package tn.esprit.soa.soap.service;

import jakarta.jws.WebService;
import org.springframework.stereotype.Component;

/**
 * Stock SIMULE : aucune API publique gratuite ne donne le stock par pharmacie.
 * Le resultat est deterministe (meme medicament + meme pharmacie = meme stock).
 */
@Component
@WebService(
        endpointInterface = "tn.esprit.soa.soap.service.DisponibiliteService",
        targetNamespace = "http://soap.medilocate.tn/",
        serviceName = "DisponibiliteService",
        portName = "DisponibilitePort")
public class DisponibiliteServiceImpl implements DisponibiliteService {

    @Override
    public DisponibiliteResponse verifierDisponibilite(String medicamentNom, String pharmacieId) {
        if (medicamentNom == null || medicamentNom.isBlank()) {
            return new DisponibiliteResponse(medicamentNom, pharmacieId, false, 0, "Nom du medicament obligatoire");
        }

        String nom = medicamentNom.toLowerCase().trim();
        String pharmacie = (pharmacieId == null || pharmacieId.isBlank()) ? "inconnue" : pharmacieId.trim();

        int seed = Math.abs((nom + "|" + pharmacie).hashCode() % 1_000_000);
        // 1 chance sur 4 d'etre en rupture, sinon 1 a 20 boites
        int quantite = (seed % 4 == 0) ? 0 : 1 + (seed / 4) % 20;

        if (quantite == 0) {
            return new DisponibiliteResponse(medicamentNom, pharmacie, false, 0, "Rupture de stock");
        }
        return new DisponibiliteResponse(medicamentNom, pharmacie, true, quantite, "En stock");
    }
}
