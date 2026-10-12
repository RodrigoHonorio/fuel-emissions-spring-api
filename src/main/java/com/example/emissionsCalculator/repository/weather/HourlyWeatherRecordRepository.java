package com.example.emissionsCalculator.repository.weather;

import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * <b>O que é:</b> Repositório JPA para a entidade HourlyWeatherRecord.
 */
@Repository
public interface HourlyWeatherRecordRepository extends JpaRepository<HourlyWeatherRecord, Long> {

    Optional<HourlyWeatherRecord> findByLocationNameAndForecastTimestamp(String locationName, LocalDateTime forecastTimestamp);

    Optional<HourlyWeatherRecord> findFirstByOrderByIdDesc();

    Optional<HourlyWeatherRecord> findFirstByOrderByForecastTimestampDesc();
}