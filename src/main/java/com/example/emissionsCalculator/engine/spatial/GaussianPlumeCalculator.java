package com.example.emissionsCalculator.engine.spatial;

import com.example.emissionsCalculator.dto.spatial.PlumePointDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GaussianPlumeCalculator {

    private static final double DEFAULT_WIND_SPEED_MS = 2.0;
    private static final double DEFAULT_WIND_DIRECTION = 0.0; // Norte

    /**
     * Calcula os pontos de dispersão atmosférica de uma fonte pontual via Modelo de Pluma Gaussiana.
     *
     * @param sourceLat Latitude do posto emissor
     * @param sourceLon Longitude do posto emissor
     * @param emissionRate Q (taxa de emissão do poluente em g/s)
     * @param windSpeedKmh Velocidade do vento em km/h
     * @param windDirectionDeg Direção de origem do vento em graus (0° a 360°)
     * @return Lista de pontos da pluma com suas respectivas concentrações e distâncias dos eixos
     */
    public List<PlumePointDTO> calculatePlume(
            double sourceLat,
            double sourceLon,
            double emissionRate,
            Double windSpeedKmh,
            Double windDirectionDeg
    ) {
        List<PlumePointDTO> plumePoints = new ArrayList<>();

        // Tratamento para vento ausente ou zerado
        double speedMs = (windSpeedKmh != null && windSpeedKmh > 0.1) ? (windSpeedKmh / 3.6) : DEFAULT_WIND_SPEED_MS;
        double dirDeg = (windDirectionDeg != null) ? windDirectionDeg : DEFAULT_WIND_DIRECTION;

        // Inverte a direção do vento para calcular a direção para onde a poluição é soprada
        double blowAngleRad = Math.toRadians((dirDeg + 180.0) % 360.0);

        int maxDistanceMeters = 800; // Alcance máximo da pluma (downwind)
        int stepMeters = 40;

        for (double x = 40; x <= maxDistanceMeters; x += stepMeters) {
            // Parâmetros empíricos de dispersão urbana Pasquill-Gifford (Classe D - Neutra)
            double sigmaY = 0.16 * x * Math.pow(1.0 + 0.0001 * x, -0.5);
            double sigmaZ = 0.14 * x * Math.pow(1.0 + 0.0003 * x, -0.5);

            for (double y = -3.0 * sigmaY; y <= 3.0 * sigmaY; y += stepMeters / 2.0) {
                // Equação Gaussiana Simplificada ao nível do solo (z = 0, h = 0)
                double concentration = (emissionRate / (Math.PI * speedMs * sigmaY * sigmaZ))
                        * Math.exp(-0.5 * Math.pow(y / sigmaY, 2.0));

                if (concentration > 0.01) {
                    // Rotação para alinhar com o vetor de vento
                    double dx = x * Math.sin(blowAngleRad) + y * Math.cos(blowAngleRad);
                    double dy = x * Math.cos(blowAngleRad) - y * Math.sin(blowAngleRad);

                    // Projeção em coordenadas geográficas
                    double pointLat = sourceLat + (dy / 111320.0);
                    double pointLon = sourceLon + (dx / (111320.0 * Math.cos(Math.toRadians(sourceLat))));

                    // Mapeia para o PlumePointDTO com 5 campos
                    plumePoints.add(new PlumePointDTO(
                            pointLat,
                            pointLon,
                            concentration,
                            x, // downwindDistanceMeters
                            y  // crosswindDistanceMeters
                    ));
                }
            }
        }

        return plumePoints;
    }
}