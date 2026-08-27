package uk.org.spire.emissionsCalculator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.constant.FuelType;
import uk.org.spire.emissionsCalculator.exception.InvalidEmissionDataException;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;
import uk.org.spire.emissionsCalculator.repository.EmissionRepository;

@Service
public class EmissionCalculationService {

    private final EmissionRepository emissionRepository;

    @Autowired
    public EmissionCalculationService(EmissionRepository emissionRepository) {
        this.emissionRepository = emissionRepository;
    }

    public PetrolStationEmission calculateAndSaveEmissions(FuelType fuelType, double volumeLitres, double temperatureCelsius) {

        // Validation rules based on real-world constraints
        if (volumeLitres <= 0) {
            throw new InvalidEmissionDataException("Fuel volume must be greater than zero.");
        }
        if (temperatureCelsius < -50 || temperatureCelsius > 60) {
            throw new InvalidEmissionDataException("Ambient temperature is out of realistic physical bounds for London.");
        }

        double tempDifference = temperatureCelsius - fuelType.getStandardTemperatureCelsius();
        double temperatureModifier = 1.0 + (tempDifference * fuelType.getTemperatureModifierRate());

        double totalVocEmissionsKg = volumeLitres * fuelType.getBaseEmissionFactor() * temperatureModifier;

        PetrolStationEmission emissionRecord = new PetrolStationEmission(
                volumeLitres,
                temperatureCelsius,
                totalVocEmissionsKg
        );

        return emissionRepository.save(emissionRecord);
    }
}