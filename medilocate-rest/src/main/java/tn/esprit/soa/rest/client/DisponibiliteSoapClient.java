package tn.esprit.soa.rest.client;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Client SOAP "a la main" : envoie une enveloppe SOAP 1.1 en HTTP POST
 * au service emedilocate-soap et lit la reponse XML (aucune librairie SOAP necessaire).
 */
@Component
public class DisponibiliteSoapClient {

    private final RestClient http = RestClient.create();
    private final String soapUrl;

    public DisponibiliteSoapClient(@Value("${soap.url}") String soapUrl) {
        this.soapUrl = soapUrl;
    }

    public Map<String, Object> verifier(String medicament, String pharmacieId) {
        String enveloppe = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:med="http://soap.medilocate.tn/">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <med:verifierDisponibilite>
                      <medicamentNom>%s</medicamentNom>
                      <pharmacieId>%s</pharmacieId>
                    </med:verifierDisponibilite>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(echapper(medicament), echapper(pharmacieId));

        String reponse = http.post()
                .uri(soapUrl)
                .contentType(MediaType.TEXT_XML)
                .header("SOAPAction", "")
                .body(enveloppe)
                .retrieve()
                .body(String.class);

        return lire(reponse);
    }

    private Map<String, Object> lire(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = f.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("medicament", texte(doc, "medicament"));
            m.put("pharmacieId", texte(doc, "pharmacieId"));
            m.put("disponible", Boolean.parseBoolean(texte(doc, "disponible")));
            String q = texte(doc, "quantite");
            m.put("quantite", q.isEmpty() ? 0 : Integer.parseInt(q));
            m.put("message", texte(doc, "message"));
            return m;
        } catch (Exception e) {
            throw new IllegalStateException("Reponse SOAP illisible : " + e.getMessage(), e);
        }
    }

    private String texte(Document doc, String nom) {
        NodeList nl = doc.getElementsByTagNameNS("*", nom);
        return nl.getLength() == 0 ? "" : nl.item(0).getTextContent();
    }

    private String echapper(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
