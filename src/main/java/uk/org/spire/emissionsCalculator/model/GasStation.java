package uk.org.spire.emissionsCalculator.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "gas_stations")
public class GasStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "osm_id")
    private Long osmId;

    private String name;
    private String operator;
    private double latitude;
    private double longitude;

    @Column(name = "number_of_pumps")
    private Integer numberOfPumps;

    @Column(name = "daily_throughput")
    private Double dailyThroughput;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    public GasStation() {
    }

    public GasStation(Long osmId, String name, String operator, double latitude, double longitude) {
        this.osmId = osmId;
        this.name = name;
        this.operator = operator;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GasStation(Long id, String name, double latitude, double longitude, Integer numberOfPumps, Double dailyThroughput) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numberOfPumps = numberOfPumps;
        this.dailyThroughput = dailyThroughput;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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

    public Integer getNumberOfPumps() {
        return numberOfPumps;
    }

    public void setNumberOfPumps(Integer numberOfPumps) {
        this.numberOfPumps = numberOfPumps;
    }

    // Métodos alias para compatibilidade com DTOs e serviços
    public Integer getPumpCount() {
        return numberOfPumps;
    }

    public void setPumpCount(Integer pumpCount) {
        this.numberOfPumps = pumpCount;
    }

    public Double getDailyThroughput() {
        return dailyThroughput;
    }

    public void setDailyThroughput(Double dailyThroughput) {
        this.dailyThroughput = dailyThroughput;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}