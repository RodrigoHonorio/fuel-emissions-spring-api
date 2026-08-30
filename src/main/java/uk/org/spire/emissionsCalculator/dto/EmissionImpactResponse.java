package uk.org.spire.emissionsCalculator.dto;

public class EmissionImpactResponse {

    private String fuelType;
    private double fuelVolume;
    private double totalEmissionsKg;
    private String closestStationName;
    private double distanceKm;

    // Getters e Setters
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public double getFuelVolume() { return fuelVolume; }
    public void setFuelVolume(double fuelVolume) { this.fuelVolume = fuelVolume; }

    public double getTotalEmissionsKg() { return totalEmissionsKg; }
    public void setTotalEmissionsKg(double totalEmissionsKg) { this.totalEmissionsKg = totalEmissionsKg; }

    public String getClosestStationName() { return closestStationName; }
    public void setClosestStationName(String closestStationName) { this.closestStationName = closestStationName; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
}