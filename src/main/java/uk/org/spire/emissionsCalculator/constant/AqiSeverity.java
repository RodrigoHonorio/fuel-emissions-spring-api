package uk.org.spire.emissionsCalculator.constant;

/**
 * Air quality bands reported by monitoring stations, together with the colours
 * used by any client rendering the station.
 */
public enum AqiSeverity {

    LOW("#198754", "#ffffff"),
    MODERATE("#ffc107", "#000000"),
    HIGH("#dc3545", "#ffffff");

    private final String markerColour;
    private final String textColour;

    AqiSeverity(String markerColour, String textColour) {
        this.markerColour = markerColour;
        this.textColour = textColour;
    }

    public static AqiSeverity fromStatus(String aqiStatus) {
        if (aqiStatus == null) {
            return LOW;
        }
        return switch (aqiStatus.trim().toUpperCase()) {
            case "HIGH", "POOR", "VERY HIGH" -> HIGH;
            case "MODERATE" -> MODERATE;
            default -> LOW;
        };
    }

    public String getMarkerColour() {
        return markerColour;
    }

    public String getTextColour() {
        return textColour;
    }
}
