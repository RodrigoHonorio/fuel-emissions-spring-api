package com.example.emissionsCalculator.repository.airQualityStation;

import com.example.emissionsCalculator.entity.airQualityStation.HourlyAirQualityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <b>O que é:</b> Repositório JPA para a entidade HourlyAirQualityRecord.
 * <b>Para que serve:</b> Persistência e busca de séries temporais de poluição.
 */
@Repository
public interface HourlyAirQualityRecordRepository extends JpaRepository<HourlyAirQualityRecord, Long> {
}