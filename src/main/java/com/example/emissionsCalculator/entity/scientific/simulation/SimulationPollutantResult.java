package com.example.emissionsCalculator.entity.scientific.simulation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_pollutant_results", indexes = {
        @Index(name = "idx_sim_result_batch_pollutant", columnList = "simulation_batch_id, target_pollutant, step_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationPollutantResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_batch_id", nullable = false)
    private SimulationBatch simulationBatch;

    @Column(name = "gas_station_id", nullable = false)
    private Long gasStationId;

    @Column(name = "step_timestamp", nullable = false)
    private LocalDateTime stepTimestamp;

    @Column(name = "rule_id", nullable = false, length = 100)
    private String ruleId;

    @Column(name = "target_pollutant", nullable = false, length = 50)
    private String targetPollutant; // VOC, BENZENE, NOX, PM10, etc.

    @Column(name = "calculated_value", nullable = false)
    private Double calculatedValue;

    @Column(name = "baseline_value", nullable = false)
    private Double baselineValue;

    @Column(name = "delta_value", nullable = false)
    private Double deltaValue;

    @Column(name = "delta_percentage", nullable = false)
    private Double deltaPercentage;

    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure;
}
