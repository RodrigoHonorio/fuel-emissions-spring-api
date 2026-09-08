package uk.org.spire.emissionsCalculator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "spatial_nodes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_station_name", columnNames = "station_name")
})
public class SpatialNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "station_name", nullable = false, unique = true, length = 150)
    private String stationName;

    @Column(name = "coordinates", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point coordinates;

    // Status da Qualidade do Ar (LOW, MODERATE, HIGH)
    @Column(name = "aqi_status", length = 50)
    private String aqiStatus = "LOW";

    protected SpatialNode() {
        // Required by Hibernate
    }

    public SpatialNode(String stationName, Point coordinates) {
        this.stationName = stationName;
        this.coordinates = coordinates;
        this.aqiStatus = "LOW";
    }

    public SpatialNode(String stationName, Point coordinates, String aqiStatus) {
        this.stationName = stationName;
        this.coordinates = coordinates;
        this.aqiStatus = aqiStatus != null ? aqiStatus : "LOW";
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public String getAqiStatus() {
        return aqiStatus;
    }

    public void setAqiStatus(String aqiStatus) {
        this.aqiStatus = aqiStatus;
    }

    // --- Compatibility Aliases for Impact Service ---

    public String getName() {
        return this.stationName;
    }

    public Point getLocation() {
        return this.coordinates;
    }
}