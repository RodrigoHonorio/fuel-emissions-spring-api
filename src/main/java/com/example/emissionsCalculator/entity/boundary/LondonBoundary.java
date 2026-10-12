package com.example.emissionsCalculator.entity.boundary;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Entidade JPA para os limites geográficos dos distritos de Londres.
 * <b>Para que serve:</b> Persistir os polígonos GeoJSON dos 33 boroughs de Londres na tabela 'london_boundaries'.
 */
@Entity
@Table(name = "london_boundaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LondonBoundary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "borough_code", nullable = false, unique = true, length = 50)
    private String boroughCode;

    @Column(name = "borough_name", nullable = false)
    private String boroughName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geojson_geometry", columnDefinition = "jsonb", nullable = false)
    private String geojsonGeometry;

    private LocalDateTime updatedAt;
}