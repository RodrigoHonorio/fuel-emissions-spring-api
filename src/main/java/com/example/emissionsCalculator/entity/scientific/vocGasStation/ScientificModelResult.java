package com.example.emissionsCalculator.entity.scientific.vocGasStation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Entidade isolada para registo de estudos e modelos científicos experimentais.
 * <b>Para que serve:</b> Armazenar resultados do módulo 'vocGasStation' sem alterar o núcleo do projeto.
 */
@Entity
@Table(name = "scientific_model_results", indexes = {
        @Index(name = "idx_model_results_station_time", columnList = "gas_station_id, model_name, calculation_timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScientificModelResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_station_id", nullable = false)
    private Long gasStationId;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "calculation_timestamp", nullable = false)
    private LocalDateTime calculationTimestamp;

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

    @Column(name = "gasoline_throughput_liters", nullable = false)
    private Double gasolineThroughputLiters;

    @Column(name = "ethanol_throughput_liters", nullable = false)
    private Double ethanolThroughputLiters;

    @Column(name = "refueling_voc_kg", nullable = false)
    private Double refuelingVocKg;

    @Column(name = "calculated_total_voc_kg", nullable = false)
    private Double calculatedTotalVocKg;

    @Column(name = "baseline_voc_kg", nullable = false)
    private Double baselineVocKg;

    @Column(name = "delta_voc_kg", nullable = false)
    private Double deltaVocKg;

    @Column(name = "delta_percentage", nullable = false)
    private Double deltaPercentage;
}
