package uk.org.spire.emissionsCalculator.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.org.spire.emissionsCalculator.constant.AqiSeverity;
import uk.org.spire.emissionsCalculator.constant.SpireConstants;
import uk.org.spire.emissionsCalculator.dto.SpatialNodeResponse;
import uk.org.spire.emissionsCalculator.model.SpatialNode;
import uk.org.spire.emissionsCalculator.repository.SpatialNodeRepository;

import java.util.List;
import java.util.Locale;

/**
 * Service layer responsible for spatial operations and geographic data translation.
 * <p>
 * It converts standard latitude and longitude inputs into JTS {@link Point}
 * geometries for persistence.
 * </p>
 */
@Service
public class SpatialNodeService {

    private static final String DEFAULT_STATION_NAME = "Estação de Monitoramento";

    private final SpatialNodeRepository repository;
    private final WeatherService weatherService;
    private final GeometryFactory geometryFactory;

    /**
     * Constructs the Spatial Node Service.
     *
     * @param repository     The data access object for spatial nodes.
     * @param weatherService The provider of live meteorological readings for London.
     */
    public SpatialNodeService(SpatialNodeRepository repository, WeatherService weatherService) {
        this.repository = repository;
        this.weatherService = weatherService;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), SpireConstants.WGS84_SRID);
    }

    /**
     * Retrieves every node located inside Greater London, enriched with the air
     * quality classification and the current meteorological readings.
     *
     * @return The nodes ready to be rendered by any client.
     */
    public List<SpatialNodeResponse> findAllLondonNodes() {
        WeatherService.WeatherData weather = weatherService.fetchCurrentWeatherForLondon();

        return repository.findAll().stream()
                // JTS maps Y to latitude and X to longitude
                .filter(node -> SpireConstants.isWithinLondon(node.getCoordinates().getY(), node.getCoordinates().getX()))
                .map(node -> toResponse(node, weather))
                .toList();
    }

    private SpatialNodeResponse toResponse(SpatialNode node, WeatherService.WeatherData weather) {
        String aqiStatus = (node.getAqiStatus() == null ? "LOW" : node.getAqiStatus()).trim().toUpperCase(Locale.UK);
        AqiSeverity severity = AqiSeverity.fromStatus(aqiStatus);
        String stationName = (node.getStationName() != null && !node.getStationName().isBlank())
                ? node.getStationName()
                : DEFAULT_STATION_NAME;

        return new SpatialNodeResponse(
                node.getId(),
                stationName,
                node.getCoordinates().getY(),
                node.getCoordinates().getX(),
                aqiStatus,
                severity,
                severity.getMarkerColour(),
                severity.getTextColour(),
                weather.temp,
                weather.windSpeed,
                weather.windDirection
        );
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
