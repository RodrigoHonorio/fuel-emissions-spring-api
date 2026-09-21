package uk.org.spire.emissionsCalculator.service;

import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.dto.GasStationDTO;
import uk.org.spire.emissionsCalculator.model.GasStation;
import uk.org.spire.emissionsCalculator.repository.GasStationRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GasStationService {

    private final GasStationRepository gasStationRepository;

    public GasStationService(GasStationRepository gasStationRepository) {
        this.gasStationRepository = gasStationRepository;
    }

    public List<GasStationDTO> getAllLondonGasStations(double temp, double volume, double humidity, double pressure, double solarRadiation, double windSpeed, String fuelType) {
        List<GasStationDTO> dtoList = new ArrayList<>();
        List<GasStation> stations = gasStationRepository.findAll();

        for (GasStation station : stations) {
            int numberOfPumps = (station.getNumberOfPumps() != null && station.getNumberOfPumps() > 0)
                    ? station.getNumberOfPumps()
                    : 4;

            double vocEmission = calculateAdvancedVoc(numberOfPumps, temp, humidity, pressure, solarRadiation, fuelType);

            dtoList.add(new GasStationDTO(
                    station.getName(),
                    station.getOperator(),
                    station.getLatitude(),
                    station.getLongitude(),
                    numberOfPumps,
                    vocEmission
            ));
        }

        return dtoList;
    }

    private double calculateAdvancedVoc(int pumps, double temp, double humidity, double pressure, double solarRadiation, String fuelType) {
        double litersPerPumpPerDay = 1250.0;

        // Fatores base oficiais EPA AP-42
        double baseEmissionGramPerLiter;
        double vaporRecoveryEfficiency;

        String type = (fuelType == null) ? "both" : fuelType.toLowerCase();

        if (type.equals("diesel")) {
            baseEmissionGramPerLiter = 0.05; // Diesel emite 98% a menos de VOC
            vaporRecoveryEfficiency = 0.50; // Menor exigência de recuperação para diesel
        } else if (type.equals("petrol")) {
            baseEmissionGramPerLiter = 2.67; // Gasolina padrão
            vaporRecoveryEfficiency = 0.92; // 92% recuperação VRU
        } else {
            // "Both" (Média ponderada combinada: 70% Gasolina / 30% Diesel no mercado urbano)
            baseEmissionGramPerLiter = (2.67 * 0.70) + (0.05 * 0.30); // ~1.88 g/L
            vaporRecoveryEfficiency = 0.85;
        }

        // 1. Fator Térmico
        double tempFactor = Math.max(0.5, 1.0 + (0.025 * (temp - 15.0)));
        // 2. Fator de Pressão Barométrica
        double pressureFactor = 1013.25 / Math.max(pressure, 850.0);
        // 3. Fator de Umidade
        double humidityFactor = 1.0 - (0.0003 * (humidity - 50.0));
        // 4. Fator de Radiação Solar
        double solarFactor = 1.0 + (0.0001 * Math.max(solarRadiation, 0.0));

        // 5. Ciclo Diurno (Diurnal Cycle baseado na hora atual do servidor)
        int currentHour = LocalTime.now().getHour();
        double diurnalFactor = 1.0 + 0.4 * Math.sin(((currentHour - 6.0) * 2.0 * Math.PI) / 24.0);

        double dailyLiters = pumps * litersPerPumpPerDay;
        double uncontrolledGram = dailyLiters * baseEmissionGramPerLiter * tempFactor * pressureFactor * humidityFactor * solarFactor * diurnalFactor;
        double controlledGram = uncontrolledGram * (1.0 - vaporRecoveryEfficiency);

        double kgPerDay = controlledGram / 1000.0;
        return Math.round(kgPerDay * 100.0) / 100.0;
    }
}