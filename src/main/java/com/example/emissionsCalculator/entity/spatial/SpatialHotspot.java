package com.example.emissionsCalculator.entity.spatial;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "spatial_hotspots",
        indexes = {
                @Index(name = "idx_spatial_hotspots_timestamp", columnList = "calculation_timestamp"),
                @Index(name = "idx_spatial_hotspots_pollutant", columnList = "pollutant_type"),
                @Index(name = "idx_spatial_hotspots_severity", columnList = "severity_level"),
                @Index(name = "idx_spatial_hotspots_institution", columnList = "source_institution")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpatialHotspot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pollutant_type", nullable = false, length = 50)
    private String pollutantType;

    @Column(name = "calculation_timestamp", nullable = false)
    private LocalDateTime calculationTimestamp;

    @Column(name = "intensity_score", nullable = false)
    private Double intensityScore;

    @Column(name = "severity_level", nullable = false, length = 30)
    private String severityLevel;

    @Column(name = "source_institution", nullable = false, length = 50)
    private String sourceInstitution;

    @Column(name = "standard_reference", length = 255)
    private String standardReference;

    @Column(name = "geometry", nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point geometry;

    @Column(name = "borough_code", length = 50)
    private String boroughCode;
}