package com.example.emissionsCalculator.entity.airQualityStation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Entity
@Table(name = "air_quality_stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_code", unique = true, nullable = false)
    private String siteCode;

    @Column(name = "name")
    private String name;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @OneToMany(mappedBy = "airQualityStation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HourlyAirQualityRecord> measurements;
}