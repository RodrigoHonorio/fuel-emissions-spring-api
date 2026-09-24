package uk.org.spire.emissionsCalculator.service;

import org.springframework.stereotype.Component;

/**
 * Modelo Matemático de Dispersão Atmosférica de Pluma Gaussiana (EPA SCREEN3 / ISC3 Standard).
 *
 * Referências Científicas e Normativas:
 * 1. United States Environmental Protection Agency (US EPA) - SCREEN3 Model User's Guide (EPA-454/R-95-004):
 *    Link: https://www.epa.gov/scram/air-quality-dispersion-modeling-screening-models
 * 2. Briggs, G.A. (1973). "Diffusion Estimation for Small Emissions". ATDL Contribution No. 79, NOAA.
 *    Link: https://www.swmg.in/blog/gaussian-plume-model-environmental-science
 * 3. Atmospheric Dispersion Modeling - Pasquill-Gifford Dispersion Parameters:
 *    Link: https://en.wikipedia.org/wiki/Atmospheric_dispersion_modeling
 */
@Component
public class GaussianPlumeModel {

    // Altura efetiva de emissão do posto de combustível (altura da bomba / tanque do veículo)
    private static final double EFFECTIVE_STACK_HEIGHT_H = 1.5; // metros

    /**
     * Calcula a concentração de COV (em µg/m³) num ponto recetor (x, y) ao nível do solo.
     *
     * @param emissionKgPerDay Taxa de emissão diária do posto em kg/dia
     * @param downwindX Distância a favor do vento (metros, deve ser > 0)
     * @param crosswindY Distância perpendicular à direção do vento (metros)
     * @param windSpeed Velocidade do vento (m/s)
     * @return Concentração em µg/m³
     */
    public double calculateConcentration(double emissionKgPerDay, double downwindX, double crosswindY, double windSpeed) {
        // Se o ponto está atrás do posto na direção oposta ao vento, a concentração é desprezível
        if (downwindX <= 0.5) {
            return 0.0;
        }

        // 1. Converter emissão de kg/dia para Q (gramas por segundo - g/s)
        // Fórmula: Q = (kg/dia * 1000 g/kg) / 86400 segundos/dia
        // Referência EPA SCREEN3 Section 2.1
        double Q = (emissionKgPerDay * 1000.0) / 86400.0;

        // 2. Ajuste de segurança para velocidade do vento (mínimo de 0.5 m/s para evitar divisão por zero)
        double u = Math.max(windSpeed, 0.5);

        // 3. Parâmetros de Dispersão de Pasquill-Gifford-Briggs para ambiente urbano (Classe D - Neutra)
        // Referência: Briggs (1973) urban formulas:
        // sigma_y = 0.16 * x * (1 + 0.0004 * x)^(-0.5)
        // sigma_z = 0.14 * x * (1 + 0.0003 * x)^(-0.5)
        double sigmaY = 0.16 * downwindX * Math.pow(1.0 + 0.0004 * downwindX, -0.5);
        double sigmaZ = 0.14 * downwindX * Math.pow(1.0 + 0.0003 * downwindX, -0.5);

        // 4. Componente da Pluma 横向 (Crosswind Gaussian Term)
        // Termo: exp( -y² / (2 * sigma_y²) )
        double crosswindTerm = Math.exp(-Math.pow(crosswindY, 2) / (2.0 * Math.pow(sigmaY, 2)));

        // 5. Componente da Pluma Vertical ao Nível do Solo (z = 0) com reflexão no solo
        // Termo: 2 * exp( -H² / (2 * sigma_z²) )
        double verticalTerm = 2.0 * Math.exp(-Math.pow(EFFECTIVE_STACK_HEIGHT_H, 2) / (2.0 * Math.pow(sigmaZ, 2)));

        // 6. Equação Completa da Concentração Gaussiana: C = (Q / (2 * PI * u * sigma_y * sigma_z)) * Termo_Y * Termo_Z
        // Unidade original: g/m³ -> Multiplica por 1.000.000 para converter para µg/m³
        // Link: https://en.wikipedia.org/wiki/Atmospheric_dispersion_modeling
        double concentrationGPerM3 = (Q / (2.0 * Math.PI * u * sigmaY * sigmaZ)) * crosswindTerm * verticalTerm;
        double concentrationUgPerM3 = concentrationGPerM3 * 1_000_000.0;

        return Double.isNaN(concentrationUgPerM3) ? 0.0 : concentrationUgPerM3;
    }
}
