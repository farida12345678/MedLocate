package tn.esprit.soa.rest.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** OpenWeatherMap si une cle est fournie, sinon Open-Meteo (gratuit, sans cle). */
@Component
public class MeteoClient {

    private final RestClient http = RestClient.create();
    private final String openWeatherKey;

    public MeteoClient(@Value("${openweather.key:}") String openWeatherKey) {
        this.openWeatherKey = openWeatherKey;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> meteo(double lat, double lon) {
        if (openWeatherKey != null && !openWeatherKey.isBlank()) {
            Map<String, Object> res = http.get()
                    .uri("https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&lang=fr&appid={key}",
                            lat, lon, openWeatherKey)
                    .retrieve()
                    .body(Map.class);
            return Map.of("source", "OpenWeatherMap", "donnees", res);
        }
        Map<String, Object> res = http.get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current_weather=true",
                        lat, lon)
                .retrieve()
                .body(Map.class);
        return Map.of("source", "Open-Meteo", "donnees", res);
    }
}
