package com.example.emissionsCalculator.controller.boundary;

import com.example.emissionsCalculator.service.boundary.enums.MapLayer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * <b>O que é:</b> Controlador REST para fornecimento de configurações visuais de mapas.
 * <b>Para que serve:</b> Expor o endpoint GET /api/maps/layers para o frontend listar dinamicamente os mapas disponíveis.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/maps")
public class MapConfigController {

    public record MapLayerDTO(String id, String name, String urlTemplate, String attribution, int maxZoom) {}

    @GetMapping("/layers")
    public ResponseEntity<List<MapLayerDTO>> getAvailableLayers() {
        List<MapLayerDTO> layers = Arrays.stream(MapLayer.values())
                .map(layer -> new MapLayerDTO(
                        layer.getId(),
                        layer.getName(),
                        layer.getUrlTemplate(),
                        layer.getAttribution(),
                        layer.getMaxZoom()
                ))
                .toList();

        return ResponseEntity.ok(layers);
    }
}