package tn.esprit.soa.soap.service;

/** Objet renvoye par le service SOAP (serialise en XML par JAXB). */
public class DisponibiliteResponse {

    private String medicament;
    private String pharmacieId;
    private boolean disponible;
    private int quantite;
    private String message;

    public DisponibiliteResponse() {
    }

    public DisponibiliteResponse(String medicament, String pharmacieId, boolean disponible, int quantite, String message) {
        this.medicament = medicament;
        this.pharmacieId = pharmacieId;
        this.disponible = disponible;
        this.quantite = quantite;
        this.message = message;
    }

    public String getMedicament() { return medicament; }
    public void setMedicament(String medicament) { this.medicament = medicament; }

    public String getPharmacieId() { return pharmacieId; }
    public void setPharmacieId(String pharmacieId) { this.pharmacieId = pharmacieId; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
