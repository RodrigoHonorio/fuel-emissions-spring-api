package uk.org.spire.emissionsCalculator.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

/**
 * Entidade JPA que representa o registro de emissões de uma estação de combustível,
 * incluindo dados meteorológicos, cálculos de VOC e localização espacial (PostGIS).
 */
@Entity
@Table(name = "petrol_station_emissions")
public class PetrolStationEmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fuel_volume_litres", nullable = false)
    private double fuelVolumeLitres;

    @Column(name = "ambient_temperature_celsius", nullable = false)
    private double ambientTemperatureCelsius;

    @Column(name = "calculated_voc_emissions_kg", nullable = false)
    private double calculatedVocEmissionsKg;

    // Coluna espacial PostGIS mapeada via Hibernate Spatial e JTS Point
    @Column(name = "location", columnDefinition = "geometry(Point, 4326)")
    private Point location;

    // Construtor vazio obrigatório pelo JPA
    public PetrolStationEmission() {
    }

    // Construtor com parâmetros utilizado no Service
    public PetrolStationEmission(double fuelVolumeLitres, double ambientTemperatureCelsius, double calculatedVocEmissionsKg, Point location) {
        this.fuelVolumeLitres = fuelVolumeLitres;
        this.ambientTemperatureCelsius = ambientTemperatureCelsius;
        this.calculatedVocEmissionsKg = calculatedVocEmissionsKg;
        this.location = location;
    }

    // Construtor legado (caso algum teste antigo use apenas 3 parâmetros)
    public PetrolStationEmission(double fuelVolumeLitres, double ambientTemperatureCelsius, double calculatedVocEmissionsKg) {
        this.fuelVolumeLitres = fuelVolumeLitres;
        this.ambientTemperatureCelsius = ambientTemperatureCelsius;
        this.calculatedVocEmissionsKg = calculatedVocEmissionsKg;
    }

    // Getters e Setters
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

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}