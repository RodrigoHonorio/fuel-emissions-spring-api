package com.example.emissionsCalculator.entity.airQualityStation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>O que é:</b> Entidade JPA para estações físicas de monitoramento de qualidade do ar.
 * <b>Para que serve:</b> Armazenar metadados fixos de localização geográfica da rede LAQN.
 * <b>De onde veio:</b> Tabela 'air_quality_stations'.
 */
@Entity
@Table(name = "air_quality_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirQualityStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_code", nullable = false, unique = true, length = 50)
    private String siteCode;

    @Column(name = "site_name", nullable = false)
    private String siteName;

    @Column(name = "site_type", length = 100)
    private String siteType;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double latitudeWGS84;

    private Double longitudeWGS84;

    @Column(name = "in_service_status")
    private String inServiceStatus;

    @Column(name = "local_authority_code")
    private String localAuthorityCode;

    @Column(name = "local_authority_name")
    private String localAuthorityName;

    @Column(name = "data_owner")
    private String dataOwner;

    @Column(name = "data_manager")
    private String dataManager;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HourlyAirQualityRecord> measurements = new ArrayList<>();
}