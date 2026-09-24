package uk.org.spire.emissionsCalculator.service;

import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.dto.GasStationDTO;
import uk.org.spire.emissionsCalculator.model.GasStation;
import uk.org.spire.emissionsCalculator.repository.GasStationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GasStationService {

    private final GasStationRepository gasStationRepository;
    private final EmissionCalculationService emissionCalculationService;

    public GasStationService(GasStationRepository gasStationRepository, EmissionCalculationService emissionCalculationService) {
        this.gasStationRepository = gasStationRepository;
        this.emissionCalculationService = emissionCalculationService;
    }

    public List<GasStationDTO> getAllLondonGasStations(double temp, double elevation, double humidity, double pressure, double solarRad, double windSpeed, String fuelType) {
        List<GasStation> stations = gasStationRepository.findAll();

        return stations.stream().map(station -> {
            double vocEmission = emissionCalculationService.calculateVocForStation(
                    station, temp, humidity, pressure, solarRad, windSpeed, fuelType
            );

            return new GasStationDTO(
                    station.getId(),
                    station.getName(),
                    station.getLatitude(),
                    station.getLongitude(),
                    station.getPumpCount(),
                    station.getDailyThroughput(),
                    vocEmission
            );
        }).collect(Collectors.toList());
    }

    public List<GasStation> getAllStationsFromDb() {
        return gasStationRepository.findAll();
    }

    public GasStation saveStation(GasStation station) {
        return gasStationRepository.save(station);
    }
}