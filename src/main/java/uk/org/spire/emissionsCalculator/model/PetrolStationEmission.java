package uk.org.spire.emissionsCalculator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Represents a fuel emission record in the database.
 * <p>
 * This entity stores the input parameters and the calculated Volatile Organic Compounds (VOC)
 * emissions for a specific refuelling event at a petrol station.
 * </p>
 */
@Entity
@Table(name = "petrol_station_emissions")
public class PetrolStationEmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double fuelVolumeLitres;
    private double ambientTemperatureCelsius;
    private double calculatedVocEmissionsKg;
    private LocalDateTime calculationTimestamp;

    // Constructors
    public PetrolStationEmission() {
    }

    public PetrolStationEmission(double fuelVolumeLitres, double ambientTemperatureCelsius, double calculatedVocEmissionsKg) {
        this.fuelVolumeLitres = fuelVolumeLitres;
        this.ambientTemperatureCelsius = ambientTemperatureCelsius;
        this.calculatedVocEmissionsKg = calculatedVocEmissionsKg;
        this.calculationTimestamp = LocalDateTime.now();
    }

    // Getters and Setters omitted for brevity (generate them in your IDE)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getFuelVolumeLitres() {
        return fuelVolumeLitres;
    }

    public void setFuelVolumeLitres(double fuelVolumeLitres) {
        this.fuelVolumeLitres = fuelVolumeLitres;
    }

    public double getAmbientTemperatureCelsius() {
        return ambientTemperatureCelsius;
    }

    public void setAmbientTemperatureCelsius(double ambientTemperatureCelsius) {
        this.ambientTemperatureCelsius = ambientTemperatureCelsius;
    }

    public double getCalculatedVocEmissionsKg() {
        return calculatedVocEmissionsKg;
    }

    public void setCalculatedVocEmissionsKg(double calculatedVocEmissionsKg) {
        this.calculatedVocEmissionsKg = calculatedVocEmissionsKg;
    }

    public LocalDateTime getCalculationTimestamp() {
        return calculationTimestamp;
    }

    public void setCalculationTimestamp(LocalDateTime calculationTimestamp) {
        this.calculationTimestamp = calculationTimestamp;
    }
}