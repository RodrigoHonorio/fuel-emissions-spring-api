package com.example.emissionsCalculator.repository.airQualityStation;

import com.example.emissionsCalculator.entity.airQualityStation.AirQualityStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <b>O que é:</b> Repositório JPA para a entidade AirQualityStation.
 * <b>Para que serve:</b> Consultas otimizadas por siteCode para upsert idempotente.
 */
@Repository
public interface AirQualityStationRepository extends JpaRepository<AirQualityStation, Long> {

    Optional<AirQualityStation> findBySiteCode(String siteCode);

    List<AirQualityStation> findBySiteCodeIn(List<String> siteCodes);
}