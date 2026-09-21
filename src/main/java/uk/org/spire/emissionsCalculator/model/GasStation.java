package uk.org.spire.emissionsCalculator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gas_stations", indexes = {
        @Index(name = "idx_gas_stations_osm_id", columnList = "osm_id", unique = true)
})
public class GasStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "osm_id", nullable = false, unique = true)
    private Long osmId;

    @Column(name = "name")
    private String name;

    @Column(name = "operator")
    private String operator;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "number_of_pumps")
    private Integer numberOfPumps = 4;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public GasStation() {}

    public GasStation(Long osmId, String name, String operator, Double latitude, Double longitude) {
        this.osmId = osmId;
        this.name = (name != null && !name.isBlank()) ? name : "Posto de Combustível";
        this.operator = (operator != null && !operator.isBlank()) ? operator : "Independente";
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOsmId() { return osmId; }
    public void setOsmId(Long osmId) { this.osmId = osmId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getNumberOfPumps() { return numberOfPumps; }
    public void setNumberOfPumps(Integer numberOfPumps) { this.numberOfPumps = numberOfPumps; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}