package uk.org.spire.emissionsCalculator.dto;

import uk.org.spire.emissionsCalculator.constant.EmissionSeverity;

public class GasStationDTO {
    private String name;
    private String operator;
    private double latitude;
    private double longitude;
    private int numberOfPumps;
    private double estimatedDailyVocKg;
    private String estimatedDailyVocLabel;
    private EmissionSeverity severity;
    private String severityLabel;
    private String markerColour;
    private String fuelLabel;

    public GasStationDTO() {}

    public GasStationDTO(String name, String operator, double latitude, double longitude, int numberOfPumps,
                         double estimatedDailyVocKg, String estimatedDailyVocLabel, EmissionSeverity severity,
                         String fuelLabel) {
        this.name = name;
        this.operator = operator;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numberOfPumps = numberOfPumps;
        this.estimatedDailyVocKg = estimatedDailyVocKg;
        this.estimatedDailyVocLabel = estimatedDailyVocLabel;
        this.severity = severity;
        this.severityLabel = severity.getLabel();
        this.markerColour = severity.getMarkerColour();
        this.fuelLabel = fuelLabel;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getNumberOfPumps() { return numberOfPumps; }
    public void setNumberOfPumps(int numberOfPumps) { this.numberOfPumps = numberOfPumps; }

    public double getEstimatedDailyVocKg() { return estimatedDailyVocKg; }
    public void setEstimatedDailyVocKg(double estimatedDailyVocKg) { this.estimatedDailyVocKg = estimatedDailyVocKg; }

    public String getEstimatedDailyVocLabel() { return estimatedDailyVocLabel; }
    public void setEstimatedDailyVocLabel(String estimatedDailyVocLabel) { this.estimatedDailyVocLabel = estimatedDailyVocLabel; }

    public EmissionSeverity getSeverity() { return severity; }
    public void setSeverity(EmissionSeverity severity) { this.severity = severity; }

    public String getSeverityLabel() { return severityLabel; }
    public void setSeverityLabel(String severityLabel) { this.severityLabel = severityLabel; }

    public String getMarkerColour() { return markerColour; }
    public void setMarkerColour(String markerColour) { this.markerColour = markerColour; }

    public String getFuelLabel() { return fuelLabel; }
    public void setFuelLabel(String fuelLabel) { this.fuelLabel = fuelLabel; }
}