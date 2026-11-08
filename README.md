# MediLocate - projet SOA

Deux projets Spring Boot :

| Projet | Port | Role |
|---|---|---|
| `emedilocate-soap` | 8087 | Service SOAP de disponibilite (stock SIMULE) |
| `medilocate-rest`  | 8080 | Service REST orchestrateur (appelle les APIs + le SOAP) |

## Lancement (ordre important)
1. Ouvrir `emedilocate-soap` dans IntelliJ -> Reload Maven -> lancer `EmedilocateSoapApplication`
   - WSDL : http://localhost:8087/soap/disponibilite?wsdl
2. Ouvrir `medilocate-rest` dans IntelliJ -> Reload Maven -> lancer `MedilocateRestApplication`

## Tests (navigateur)
- http://localhost:8080/api/disponibilite?medicament=doliprane&pharmacieId=123
- http://localhost:8080/api/medicaments?nom=doliprane
- http://localhost:8080/api/pharmacies?ville=Sfax
- http://localhost:8080/api/meteo?ville=Sfax
- **Tout en un :** http://localhost:8080/api/recherche?medicament=doliprane&ville=Sfax&rayon=3000

## APIs utilisees
- API Medicaments FR (BDPM) : https://medicaments-api.giygas.dev/v1/medicaments?search=...
- Nominatim + Overpass (OpenStreetMap) : geocodage et pharmacies
- Open-Meteo (par defaut) ou OpenWeatherMap (si `openweather.key` est renseigne)
- Service SOAP interne : stock simule (aucune API publique gratuite ne fournit le stock par pharmacie)
