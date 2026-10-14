package com.example.emissionsCalculator.entity.scientific.simulation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_batches", indexes = {
        @Index(name = "idx_simulation_batch_timestamp", columnList = "execution_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_name", nullable = false, length = 200)
    private String batchName;

    @Column(name = "article_name", length = 255)
    private String articleName;

    @Column(name = "execution_timestamp", nullable = false)
    private LocalDateTime executionTimestamp;

    @Column(name = "horizon_type", nullable = false, length = 50)
    private String horizonType; // Ex: HOURLY_24H, FORECAST_16_DAYS

    @Column(name = "lookback_days")
    private Integer lookbackDays; // Número de dias de lookback (passado) definidos pelo utilizador

    @Column(name = "history_start_time")
    private LocalDateTime historyStartTime; // Início exato do período histórico analisado

    @Column(name = "start_forecast_time", nullable = false)
    private LocalDateTime startForecastTime;

    @Column(name = "end_forecast_time", nullable = false)
    private LocalDateTime endForecastTime;

    @Column(name = "parent_simulation_id")
    private Long parentSimulationId; // Suporte ao encadeamento de cálculos

    @Column(name = "status", nullable = false, length = 30)
    private String status; // SUCCESS, PROCESSING, FAILED
}