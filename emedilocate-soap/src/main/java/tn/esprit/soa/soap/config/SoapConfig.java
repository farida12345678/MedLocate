package tn.esprit.soa.soap.config;

import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.soa.soap.service.DisponibiliteService;

@Configuration
public class SoapConfig {

    /** Publie le service sur http://localhost:8087/soap/disponibilite (WSDL : ...?wsdl) */
    @Bean
    public Endpoint disponibiliteEndpoint(Bus bus, DisponibiliteService service) {
        EndpointImpl endpoint = new EndpointImpl(bus, service);
        endpoint.publish("/disponibilite");
        return endpoint;
    }
}
