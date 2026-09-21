package uk.org.spire.emissionsCalculator.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OverpassClient {

    @Value("${osm.overpass.url:https://overpass.kumi.systems/api/interpreter}")
    private String primaryOverpassUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final List<String> FALLBACK_URLS = List.of(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.private.coffee/api/interpreter"
    );

    public String fetchGasStationsRawJson() {
        // Query limitada para a área de Grande Londres
        String query = "[out:json][timeout:60];node[\"amenity\"=\"fuel\"](51.28,-0.51,51.69,0.33);out body;";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>("data=" + query, headers);

        // Tenta a URL configurada no application.properties
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(primaryOverpassUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Espelho primário do Overpass falhou (" + primaryOverpassUrl + "). A tentar fallbacks...");
        }

        // Tenta os servidores de fallback caso o primário falhe
        for (String fallbackUrl : FALLBACK_URLS) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(fallbackUrl, request, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Fallback " + fallbackUrl + " também falhou.");
            }
        }

        return null;
    }
}