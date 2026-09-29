package uk.org.spire.emissionsCalculator.constant;

/**
 * Severity bands applied to the estimated daily VOC emission of a petrol station.
 * Each band carries the colour used by any client rendering the value.
 */
public enum EmissionSeverity {

    LOW("#28a745", "Baixa"),
    MODERATE("#ffc107", "Moderada"),
    HIGH("#fd7e14", "Alta"),
    CRITICAL("#dc3545", "Crítica");

    private static final double MODERATE_THRESHOLD_KG = 50.0;
    private static final double HIGH_THRESHOLD_KG = 150.0;
    private static final double CRITICAL_THRESHOLD_KG = 300.0;

    private final String markerColour;
    private final String label;

    EmissionSeverity(String markerColour, String label) {
        this.markerColour = markerColour;
        this.label = label;
    }

    public static EmissionSeverity fromDailyVocKg(double dailyVocKg) {
        if (dailyVocKg > CRITICAL_THRESHOLD_KG) {
            return CRITICAL;
        }
        if (dailyVocKg > HIGH_THRESHOLD_KG) {
            return HIGH;
        }
        if (dailyVocKg > MODERATE_THRESHOLD_KG) {
            return MODERATE;
        }
        return LOW;
    }

    public String getMarkerColour() {
        return markerColour;
    }

    public String getLabel() {
        return label;
    }
}
