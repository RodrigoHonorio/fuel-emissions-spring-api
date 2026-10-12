package com.example.emissionsCalculator.controller.boundary;

import com.example.emissionsCalculator.repository.boundary.LondonBoundaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * <b>O que é:</b> Controlador REST para consulta de dados geográficos dos distritos.
 * <b>Para que serve:</b> Retornar os polígonos consolidados do banco formatados em um GeoJSON padrão (`FeatureCollection`).
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/london-boundaries")
@RequiredArgsConstructor
public class LondonBoundaryController {

    private final LondonBoundaryRepository repository;

    @GetMapping(value = "/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBoundariesGeoJson() {
        var boundaries = repository.findAll();

        String featuresJson = boundaries.stream()
                .map(b -> """
                {
                  "type": "Feature",
                  "properties": {
                    "boroughCode": "%s",
                    "boroughName": "%s"
                  },
                  "geometry": %s
                }
                """.formatted(b.getBoroughCode(), b.getBoroughName(), b.getGeojsonGeometry()))
                .collect(Collectors.joining(","));

        String featureCollection = """
            {
              "type": "FeatureCollection",
              "features": [%s]
            }
            """.formatted(featuresJson);

        return ResponseEntity.ok(featureCollection);
    }
}