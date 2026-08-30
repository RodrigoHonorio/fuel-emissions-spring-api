package uk.org.spire.emissionsCalculator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Point;

/**
 * Represents a physical spatial node (e.g., an air quality monitoring station)
 * within the S.P.I.R.E. ecosystem.
 * <p>
 * This entity utilises the Java Topology Suite (JTS) to store precise geographical
 * coordinates, allowing for complex spatial queries and radius calculations
 * directly within the persistence layer.
 * </p>
 */
@Entity
@Table(name = "spatial_nodes")
public class SpatialNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * The designated name of the monitoring station or sensor node.
     */
    @Column(name = "station_name", nullable = false, length = 150)
    private String stationName;

    /**
     * The exact geographical coordinates of the node.
     * Defined as a JTS Point using the WGS 84 coordinate reference system.
     */
    @Column(name = "coordinates", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point coordinates;

    /**
     * Default constructor required by the JPA specification.
     * Protected to prevent direct instantiation without required parameters.
     */
    protected SpatialNode() {
        // Required by Hibernate
    }

    /**
     * Parameterised constructor to initialise a new Spatial Node.
     *
     * @param stationName The name of the environmental monitoring station.
     * @param coordinates The geographical location mapped as a JTS Point.
     */
    public SpatialNode(String stationName, Point coordinates) {
        this.stationName = stationName;
        this.coordinates = coordinates;
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

    // --- Compatibility Aliases for Impact Service ---

    public String getName() {
        return this.stationName;
    }

    public Point getLocation() {
        return this.coordinates;
    }
}