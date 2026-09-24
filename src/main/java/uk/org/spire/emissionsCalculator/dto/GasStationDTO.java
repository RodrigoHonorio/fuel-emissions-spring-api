package uk.org.spire.emissionsCalculator.dto;

public class GasStationDTO {

    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private Integer pumpCount;
    private Double dailyThroughput;
    private double vocEmission;

    public GasStationDTO() {
    }

    public GasStationDTO(Long id, String name, double latitude, double longitude, Integer pumpCount, Double dailyThroughput, double vocEmission) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pumpCount = pumpCount;
        this.dailyThroughput = dailyThroughput;
        this.vocEmission = vocEmission;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Integer getPumpCount() {
        return pumpCount;
    }

    public void setPumpCount(Integer pumpCount) {
        this.pumpCount = pumpCount;
    }

    public Double getDailyThroughput() {
        return dailyThroughput;
    }

    public void setDailyThroughput(Double dailyThroughput) {
        this.dailyThroughput = dailyThroughput;
    }

    public double getVocEmission() {
        return vocEmission;
    }

    public void setVocEmission(double vocEmission) {
        this.vocEmission = vocEmission;
    }
}