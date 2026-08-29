package uk.org.spire.emissionsCalculator.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.org.spire.emissionsCalculator.constant.SpireConstants;
import uk.org.spire.emissionsCalculator.model.SpatialNode;
import uk.org.spire.emissionsCalculator.repository.SpatialNodeRepository;

/**
 * Service layer responsible for spatial operations and geographic data translation.
 * <p>
 * It converts standard latitude and longitude inputs into JTS {@link Point}
 * geometries for persistence.
 * </p>
 */
@Service
public class SpatialNodeService {

    private final SpatialNodeRepository repository;
    private final GeometryFactory geometryFactory;

    /**
     * Constructs the Spatial Node Service.
     *
     * @param repository The data access object for spatial nodes.
     */
    public SpatialNodeService(SpatialNodeRepository repository) {
        this.repository = repository;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), SpireConstants.WGS84_SRID);
    }

    /**
     * Registers a new spatial node into the database.
     * <p>
     * Note: In JTS geographic coordinate systems, the X axis represents Longitude,
     * whilst the Y axis represents Latitude.
     * </p>
     *
     * @param stationName The designated name of the monitoring station.
     * @param latitude    The latitude coordinate.
     * @param longitude   The longitude coordinate.
     * @return The persisted {@link SpatialNode} entity.
     */
    @Transactional
    public SpatialNode registerNode(String stationName, double latitude, double longitude) {
        // Critical: JTS Coordinate uses (x, y) which maps strictly to (longitude, latitude)
        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point geographicalPoint = geometryFactory.createPoint(coordinate);

        SpatialNode node = new SpatialNode(stationName, geographicalPoint);
        return repository.save(node);
    }
}
