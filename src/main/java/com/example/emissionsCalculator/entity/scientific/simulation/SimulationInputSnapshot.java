package com.example.emissionsCalculator.entity.scientific.simulation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_input_snapshots", indexes = {
        @Index(name = "idx_sim_snapshot_batch_station", columnList = "simulation_batch_id, gas_station_id, step_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationInputSnapshot {

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

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

    @Column(name = "wind_speed_kmh", nullable = false)
    private Double windSpeedKmh;

    @Column(name = "relative_humidity", nullable = false)
    private Double relativeHumidity;

    @Column(name = "throughput_liters", nullable = false)
    private Double throughputLiters;

    @Column(name = "baseline_reference_value", nullable = false)
    private Double baselineReferenceValue;
}
