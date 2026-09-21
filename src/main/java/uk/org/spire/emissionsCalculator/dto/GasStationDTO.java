package uk.org.spire.emissionsCalculator.dto;

public class GasStationDTO {
    private String name;
    private String operator;
    private double latitude;
    private double longitude;
    private int numberOfPumps;
    private double estimatedDailyVocKg;

    public GasStationDTO() {}

    public GasStationDTO(String name, String operator, double latitude, double longitude, int numberOfPumps, double estimatedDailyVocKg) {
        this.name = name;
        this.operator = operator;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numberOfPumps = numberOfPumps;
        this.estimatedDailyVocKg = estimatedDailyVocKg;
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
}