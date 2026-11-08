package tn.esprit.soa.soap.service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@WebService(name = "DisponibiliteService", targetNamespace = "http://soap.medilocate.tn/")
public interface DisponibiliteService {

    @WebMethod
    @WebResult(name = "reponse")
    DisponibiliteResponse verifierDisponibilite(
            @WebParam(name = "medicamentNom") String medicamentNom,
            @WebParam(name = "pharmacieId") String pharmacieId);
}
