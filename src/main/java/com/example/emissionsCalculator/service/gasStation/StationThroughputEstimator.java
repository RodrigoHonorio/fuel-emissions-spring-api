package com.example.emissionsCalculator.service.gasStation;

import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;

/**
 * <b>O que é:</b> Estimador de tráfego e volume diário de combustíveis para postos.
 * <b>Para que serve:</b> Automatizar o cálculo real de litros com base nas características da CMA (supermercados, autoestradas, marcas e localização por postcode), eliminando valores totalmente estáticos.
 */
public class StationThroughputEstimator {

    public record FuelSplit(double gasolineLiters, double ethanolLiters, double totalLiters) {}

    /**
     * Estima o volume diário real de combustíveis com base nas características da CMA e localização.
     */
    public static FuelSplit estimateThroughput(CmaGasStation station) {
        if (station == null) {
            return new FuelSplit(4050.0, 450.0, 4500.0); // Fallback seguro integrado
        }

        double baseDailyLiters;

        // 1. Postos de Autoestrada (Motorway)
        if (Boolean.TRUE.equals(station.getIsMotorwayServiceStation())) {
            baseDailyLiters = 22000.0;
        }
        // 2. Postos de Supermercados
        else if (Boolean.TRUE.equals(station.getIsSupermarketServiceStation())) {
            baseDailyLiters = 18000.0;
        }
        // 3. Marcas principais conhecidas
        else if (station.getBrandName() != null && matchesMajorBrand(station.getBrandName())) {
            baseDailyLiters = 9000.0;
        }
        // 4. Postos independentes / menores
        else {
            baseDailyLiters = 3500.0;
        }

        // Ajuste demográfico por Postcode (Zonas centrais de Londres têm maior rotação)
        if (station.getPostcode() != null) {
            String pc = station.getPostcode().trim().toUpperCase();
            if (pc.startsWith("EC") || pc.startsWith("WC") || pc.startsWith("SW1") || pc.startsWith("W1")) {
                baseDailyLiters *= 1.25; // +25% de tráfego no centro financeiro/turístico
            }
        }

        // Proporção padrão UK: ~90% Gasolina (E10) e ~10% Etanol/Misturas corrigida
        double gasoline = baseDailyLiters * 0.90;
        double ethanol = baseDailyLiters * 0.10;

        return new FuelSplit(gasoline, ethanol, baseDailyLiters);
    }

    private static boolean matchesMajorBrand(String brand) {
        String upper = brand.toUpperCase();
        return upper.contains("BP") || upper.contains("SHELL") || upper.contains("ESSO") ||
                upper.contains("TEXACO") || upper.contains("JET");
    }
}